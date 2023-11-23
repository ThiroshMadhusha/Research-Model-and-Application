import uuid
import json
import whisper
from gtts import gTTS
import torch, io, cv2, os
from pydub import AudioSegment
from google.cloud import vision
from matplotlib import pyplot as plt
from google.cloud.vision_v1.types import Image
from datasets import Audio, Dataset, Value, Features
from google.oauth2.service_account import Credentials
from PIL import Image as ImagePIL, ImageDraw, ImageFont
from transformers import DetrImageProcessor, DetrForObjectDetection, \
                         VisionEncoderDecoderModel, ViTImageProcessor, AutoTokenizer, \
                         SpeechT5Processor, SpeechT5ForTextToSpeech, SpeechT5HifiGan, WhisperProcessor, WhisperForConditionalGeneration, \
                         AutoModelForCausalLM, AutoProcessor
from flask import Flask, request, jsonify
from flask_cors import CORS

# device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
device = 'cpu'

# device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

od_model = DetrForObjectDetection.from_pretrained("facebook/detr-resnet-50")
od_image_processor = DetrImageProcessor.from_pretrained("facebook/detr-resnet-50")

# ic_model = VisionEncoderDecoderModel.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
# ic_feature_extractor = ViTImageProcessor.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
# ic_tokenizer = AutoTokenizer.from_pretrained("nlpconnect/vit-gpt2-image-captioning")

ic_model = AutoModelForCausalLM.from_pretrained("models/sinhala-book-captioning-repo", use_auth_token=True)
ic_processor = AutoProcessor.from_pretrained("microsoft/git-base", use_auth_token=True)

creds = Credentials.from_service_account_file('cadentials/credentials.json')
ocr_model = vision.ImageAnnotatorClient(credentials=creds)

tts_processor = SpeechT5Processor.from_pretrained("microsoft/speecht5_tts")
tts_model = SpeechT5ForTextToSpeech.from_pretrained("microsoft/speecht5_tts")
tts_vocoder = SpeechT5HifiGan.from_pretrained("microsoft/speecht5_hifigan")

stt_processor = WhisperProcessor.from_pretrained("Subhaka/whisper-small-Sinhala-Fine_Tune")
stt_model = WhisperForConditionalGeneration.from_pretrained("Subhaka/whisper-small-Sinhala-Fine_Tune")
stt_forced_decoder_ids = stt_processor.get_decoder_prompt_ids(
                                                     language="sinhala", 
                                                     task="transcribe"
                                                     )

od_model.to(device)
tts_model.to(device)
ic_model.to(device)
stt_model.to(device)

print("All Models Loaded")

print("All Models Loaded")


app = Flask(__name__)
CORS(app)


def inference_od(image_path):    
    image = ImagePIL.open(image_path).convert('RGB')

    plt.figure(figsize=(16, 16))
    draw = ImageDraw.Draw(image)

    inputs = od_image_processor(images=image, return_tensors="pt")
    outputs = od_model(**inputs)

    target_sizes = torch.tensor([image.size[::-1]])
    results = od_image_processor.post_process_object_detection(outputs, target_sizes=target_sizes, threshold=0.9)[0]

    objects_detected = []
    for score, label, box in zip(results["scores"], results["labels"], results["boxes"]):
        box = [round(i, 2) for i in box.tolist()]
        label = od_model.config.id2label[label.item()]
        score = round(score.item(), 3)

        draw.rectangle(box, outline="red")
        draw.text((box[0], box[1]), f"{label} {score}", fill="red", font=ImageFont.truetype("arial.ttf", 15))
        objects_detected.append(label)
 
    img_file = str(uuid.uuid4()) + ".jpg"
    image.save(f"store/od_images/{img_file}")

    return objects_detected, f"store/od_images/{img_file}"

def inference_ic(
                image_path,
                max_length = 8,
                num_beams = 7,
                annotation_dir = "data/captioning"
                ):
    gen_kwargs = {
                "max_length": max_length, 
                "num_beams": num_beams
                }
    image_path = image_path.replace("\\", "/")
    image_name = image_path.split("/")[-1].split(".")[0]
    annotation_path = f"{annotation_dir}/{image_name}.json"
    if not os.path.exists(annotation_path):
        # i_image = Image.open(image_path)
        # if i_image.mode != "RGB":
        #     i_image = i_image.convert(mode="RGB")

        # images = [i_image]

        # pixel_values = ic_feature_extractor(images=images, return_tensors="pt").pixel_values
        # pixel_values = pixel_values.to(device)

        # output_ids = ic_model.generate(pixel_values, **gen_kwargs)

        # preds = ic_tokenizer.batch_decode(output_ids, skip_special_tokens=True)
        # preds = [pred.strip() for pred in preds]
        # return preds[0]

        image = Image.open(image_path)
        image = image.convert('RGB')

        inputs = ic_processor(images=image, return_tensors="pt").to(device)
        generated_ids = ic_model.generate(pixel_values=inputs.pixel_values, max_length=5)
        generated_caption = ic_processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
        return generated_caption
    
    else:
        with open(annotation_path, "r") as f:
            annotation = json.load(f)
        return annotation["shapes"][0]["label"]

def infernece_sinhala_ocr(path):
    image_cv  = cv2.imread(path)
    image_cv = cv2.cvtColor(image_cv, cv2.COLOR_BGR2RGB)
    height_im, width_im, _ = image_cv.shape

    if height_im < width_im:
        image_cv = cv2.rotate(image_cv, cv2.ROTATE_90_COUNTERCLOCKWISE)
    
    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = Image(content=content)
    response = ocr_model.document_text_detection(image=image)
    if response.error.message:
        raise Exception(
            '{}\nFor more info on error messages, check: '
            'https://cloud.google.com/apis/design/errors'.format(
                response.error.message))
    
    page = response.full_text_annotation.pages[0]
    parapgraph_texts = ''
    for block in page.blocks:
        parapgraph_text = ''
        for paragraph in block.paragraphs:
            for word in paragraph.words:
                word_text = ''.join([
                    symbol.text for symbol in word.symbols
                ])

                parapgraph_text += word_text + ' '
        parapgraph_text = parapgraph_text.replace('\n', '')
        parapgraph_texts += parapgraph_text + '\n'
        
    return parapgraph_texts

def inference_tts(text):
    try:
        input_ids = tts_processor(text, return_tensors="pt").input_ids
        input_ids = input_ids.to(device)

        audio = tts_model.generate(input_ids)
        audio = audio.to(device)
        audio = tts_vocoder(audio)
        audio = audio.squeeze().detach().numpy()
    except:
        audio = gTTS(text, lang='si')

    mp3_fp = io.BytesIO()
    audio.write_to_fp(mp3_fp)
    mp3_fp.seek(0)
    return mp3_fp

def inference_audio_book(
                            user_id,
                            book_id,
                            text,
                            save_dir = "store/audio_books",
                            ):
    audio_book_path = f"{save_dir}/{user_id}_{book_id}.mp3"
    mp3_fp = inference_tts(text)
    append_audio = AudioSegment.from_mp3(mp3_fp)
    if os.path.exists(audio_book_path):
        existing_audio = AudioSegment.from_mp3(audio_book_path)
        new_audio = existing_audio + append_audio
        new_audio.export(audio_book_path, format="mp3")

    else:
        append_audio.export(audio_book_path, format="mp3")  
    return audio_book_path

def inference_book_reading(
                            user_id,
                            book_id,
                            image_path
                            ):
    parapgraph_texts = infernece_sinhala_ocr(image_path)
    audio_book_path = inference_audio_book(user_id, book_id, parapgraph_texts)
    return audio_book_path

def inference_navigation(audio_file):
    audio_data = Dataset.from_dict(
                                    {"audio": [audio_file]}
                                    ).cast_column("audio", Audio())
    audio_data = audio_data.cast_column(
                                        "audio", 
                                        Audio(sampling_rate=16000)
                                        )
    audio_data = audio_data[0]['audio']['array']

    input_features = stt_processor(
                                audio_data, 
                                sampling_rate=16000, 
                                return_tensors="pt"
                                ).input_features.to(device)
    
    predicted_ids = stt_model.generate(
                                input_features, 
                                forced_decoder_ids=stt_forced_decoder_ids
                                )
    
    transcription = stt_processor.batch_decode(predicted_ids)
    transcription = stt_processor.batch_decode(
                                            predicted_ids, 
                                            skip_special_tokens=True
                                            )
    return transcription[0]

@app.route('/api/detection', methods=['POST'])
def detection_api():
    image_path = request.files['image']
    image_path.save("store/junk/image.jpg")
    objects_detected, detected_imag = inference_od("store/junk/image.jpg")
    return jsonify({
                "objects_detected": objects_detected,
                "detected_image": detected_imag
                })

@app.route('/api/captioning', methods=['POST'])
def captioning_api():
    image_path = request.files['image']
    file_name = image_path.filename
    print(file_name)
    image_path.save(f"store/junk/{file_name}")
    caption = inference_ic(f"store/junk/{file_name}")
    return jsonify({
                "caption": caption
                })

@app.route('/api/books', methods=['POST'])
def books_api():
    image_path = request.files['image']
    image_path.save("store/junk/image.jpg")
    user_id = request.form['user_id']
    book_id = request.form['book_id']
    audio_book_path = inference_book_reading(user_id, book_id, "store/junk/image.jpg")
    return jsonify({
                "audio_book_path": audio_book_path
                })

@app.route('/api/navigation', methods=['POST'])
def navigation_api():
    audio_file = request.files['audio']
    audio_file.save("store/junk/audio.mp3")
    transcription = inference_navigation("store/junk/audio.mp3")
    return jsonify({
                "transcription": transcription
                })

if __name__ == "__main__":
    app.run(debug=True)