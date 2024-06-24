from src.utils.init_path import init_path
from src.utils.preprocess import CropAndExtract
import os

config_path='/root/avatar/src/config'
checkpoint_path='/root/avatar/checkpoints'
device='cuda'
pic_path = '/root/avatar/preprocess/girl2.png'
size=256
sadTalker = init_path(checkpoint_path, config_path, 256, False, 'crop')
preprocess_model = CropAndExtract(sadTalker, device)

print("OK")
first_frame_dir = '/root/avatar/preprocess'
os.makedirs(first_frame_dir, exist_ok=True)
first_coeff_path, crop_pic_path, crop_info = preprocess_model.generate(pic_path, first_frame_dir, 'crop', True, size)
print(first_coeff_path, crop_pic_path, crop_info)