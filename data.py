import matplotlib as plt
import numpy
import dateutil.parser as dp
import requests
import time

r = requests.get('http://localhost:3000/alldata')
json = r.json()
user_email = None

dic = { }

for num in xrange(0, 23):

	hour = num
	latArr = []
	longitudeArr = []

	for i in json:
		if not user_email:
			user_email = i["user_email"]
			d = dp.parse(i["createdAt"])
			h = d.hour + d.minute / 60. + d.second / 3600.
			if hour < h < hour + 0.99999:
				dic[hour] = { "lat": i["lat"], "long": i["long"]}
		elif i["user_email"] == user_email:
			d = dp.parse(i["createdAt"])
			h = d.hour + d.minute / 60. + d.second / 3600.
			if hour < h < hour + 0.99999:
				dic[hour] = { "lat": i["lat"], "long": i["long"]}

	print dic[hour]
	for coords in dic[hour]:
		lat, longitude = coords.lat, coords.long
		latArr.append(lat)
		longitudeArr.append(longitude)

	plt.plot(latArr, numpy.poly1d(numpy.polyfit(latArr, longitudeArr, 1))(latArr))

#plt.plot(x, numpy.poly1d(numpy.polyfit(x, y, 1))(x))