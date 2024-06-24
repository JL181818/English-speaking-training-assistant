from models import SadTalker
import random 
import time
blink_every = True
size_of_image = 256
preprocess_type = 'crop'
facerender = 'facevid2vid'
enhancer = False
# enhancer = True
is_still_mode = False
pic_path = "./inputs/girl.png"
pic_path = "/root/avatar/preprocess/girl2.png"
crop_pic_path = "./inputs/first_frame_dir_girl/girl.png"
first_coeff_path = "./inputs/first_frame_dir_girl/girl.mat"
crop_info = ((403, 403), (19, 30, 502, 513), [40.05956541381802, 40.17324339233366, 443.7892505041507, 443.9029284826663])

exp_weight = 1
batch_size = 10
pose_style = random.randint(0, 45)
use_ref_video = False
ref_video = None
ref_info = 'pose'
use_idle_mode = False
length_of_audio = 5
audio = './files/audio.wav'


talker = SadTalker()
start_time = time.time()

video = talker.test2(
            source_image=pic_path,
            driven_audio=audio,
            preprocess=preprocess_type, 
            still_mode=is_still_mode,
            use_enhancer=enhancer,
            batch_size=batch_size,
            size=size_of_image, 
            pose_style = pose_style, 
            facerender=facerender,
            exp_scale=exp_weight, 
            use_idle_mode = use_idle_mode,
            length_of_audio = length_of_audio,
            use_blink=blink_every, 
            fps=20
)
end_time = time.time()
elapsed_time = end_time - start_time
print(f"inference took {elapsed_time} seconds to run.")