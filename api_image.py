import os
import sys
import imutils
import random
import math
import re
import time
import numpy as np
import cv2
import json
from flask import Flask
from flask import Flask, request, redirect, jsonify
from werkzeug.utils import secure_filename
from flask import send_from_directory
from skimage.measure import compare_ssim
import random
import string


ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg'])

app = Flask(__name__)
UPLOAD_FOLDER = 'mole/images'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

ALLOWED_EXTENSIONS = set(['jpg','JPEG','png','jpeg','png'])
"""
Check file extention
"""
def allowed_file(filename):
	return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/mole/images/<path:filename>') 
def send_image(filename): 
    return send_from_directory(UPLOAD_FOLDER, filename)


"""
Main Funtion
"""
@app.route('/file', methods=['GET','POST'])
def upload_file():
	try:
		if ('image1' not in request.files) or ('image2' not in request.files):
			"""
			if no images in request
			"""
			resp = jsonify({'message' : 'No file part in the request','error':'Empty Request','statusCode':0})
			resp.status_code = 400
			return resp
		image1 = request.files['image1'] 
		image2 = request.files['image2'] 
		if image1.filename == '' or image2.filename == '':
			"""
			empy parameter
			"""
			resp = jsonify({'message' : 'No Parameter should be empty','error':'No file selected for uploading','statusCode':0})
			resp.status_code = 400
			return resp
		if image1 and image2 and allowed_file(image1.filename) and allowed_file(image2.filename):

			# generate random string and concate with file name
			random_string = ''.join(random.choices(string.ascii_uppercase +string.digits, k = 5))
			random_string_1 = ''.join(random.choices(string.ascii_uppercase +string.digits, k = 5))
			filename1 = random_string + secure_filename(image1.filename)
			filename2 = random_string_1 + secure_filename(image2.filename)
			# save file
			image1.save(os.path.join(app.config['UPLOAD_FOLDER'], filename1))
			image2.save(os.path.join(app.config['UPLOAD_FOLDER'], filename2))
			try:
				# read file
				input_image1 = cv2.imread((UPLOAD_FOLDER + '/'+filename1))
				input_image2 = cv2.imread((UPLOAD_FOLDER + '/'+filename2))
				height, width, channels = input_image1.shape
				height2, width2, channels = input_image2.shape

				if height != height2 or width != width2:
					resp = jsonify({'message' : 'file invalid','error':'dimantion should be same','statusCode':0})
					resp.status_code = 400
					return resp					

				# convert in gray scale
				grayA = cv2.cvtColor(input_image1, cv2.COLOR_BGR2GRAY)
				grayB = cv2.cvtColor(input_image2, cv2.COLOR_BGR2GRAY)
				# compair image 
				(score, diff) = compare_ssim(grayA, grayB, full=True)
				diff = (diff * 255).astype("uint8")
				# print(diff)
				# draw ract if diff
				thresh = cv2.threshold(diff, 0, 255,
					cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)[1]
				cnts = cv2.findContours(thresh.copy(), cv2.RETR_EXTERNAL,
					cv2.CHAIN_APPROX_SIMPLE)
				cnts = imutils.grab_contours(cnts)
				final_message = "Images are same"
				for c in cnts:
					final_message = "Images are different"
					(x, y, w, h) = cv2.boundingRect(c)
					cv2.rectangle(input_image1, (x, y), (x + w, y + h), (0, 0, 255), 2)
					cv2.rectangle(input_image2, (x, y), (x + w, y + h), (0, 0, 255), 2)

				outputImage = UPLOAD_FOLDER+'/'+random_string+'.jpg' 
				cv2.imwrite(outputImage,input_image1)

			except Exception as e:
				resp = jsonify({'message' : 'No file selected for uploading','error':e,'statusCode':0})
				resp.status_code = 400
				return resp

			resp = jsonify({'statusCode':1,'message':final_message,'data':{'image1':UPLOAD_FOLDER + '/'+filename1,'image2':UPLOAD_FOLDER + '/'+filename2,'outputImage':outputImage}})
			resp.status_code = 202
			return resp
		else:
			resp = jsonify({'message' : 'Allowed file types are txt, pdf, png, jpg, jpeg, gif','error':'File Note Allowded','statusCode':0})
			resp.status_code = 400
			return resp
	except Exception as e:
		resp = jsonify({'message' : 'invalid image','error':e,'statusCode':0})
		resp.status_code = 400
		return resp

if __name__ == '__main__':
	app.run(host ='145.14.157.88', port = 5001, debug = True)
