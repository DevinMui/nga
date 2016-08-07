var express = require('express')
var bodyParser = require('body-parser')
var mongoose = require('mongoose')
var ejs = require('ejs')
var compression = require('compression')
var bcrypt = require('bcrypt')
var app = express()

const saltRounds = 10

app.set('view engine', 'ejs')
app.use(express.static('public'))
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({ extended: false }))
app.use(compression()) // gzip compression less data

mongoose.connect('mongodb://localhost/nga')

var dataSchema = new mongoose.Schema({
	lat: Number,
	long: Number,
	user_email: String,
	disease: String,
	severity: Number // severity from 0 - 3 where 0 is least severe and 3 is most severe
},
{
	timestamps: true
})

var geoDataSchema = new mongoose.Schema({
	lat: Number,
	long: Number,
	email: String,
},
{
	timestamps: true
})

var userSchema = new mongoose.Schema({
	name: String, // really doesnt matter but wutever
	email: String,
	disease: String,
	severity: Number, // severity from 0 - 3 where 0 is least severe and 3 is most severe
	doctor: Boolean, 
	password: String
},
{
	timestamps: true
})

var Data = mongoose.model('Data', dataSchema)
var User = mongoose.model('User', userSchema)
var GeoData = mongoose.model('GeoData', geoDataSchema)

app.get('/', function(req, res){
	// map
	res.render('index')
})

app.get('/memes', function(req, res){
	res.redirect("https://www.youtube.com/watch?v=HgQEuPw942c")
})

app.post('/update_person', function(req, res){
	// send array with people close, including throughout sick time (typically 1 -3 days but depends on illness)
	User.findOne({ email: req.body.user.email }, function(err, doc){
		if(err){
			res.json({"error": "user could not be found"})
		} else if(doc == undefined){
			res.json({"error": "user could not be found"})
		} else if(!doc.doctor){
			res.json({"error": "user is not a doctor"})
		} else {
			User.findOne({ email: req.body.email }, function (err, doc){
				doc.disease = req.body.disease
				doc.severity = 4 // infected
				doc.save(function(err, doc){
					var data = Data({
						lat: req.body.lat,
						long: req.body.long,
						user_email: req.body.email,
						disease: req.body.disease,
						severity: 4
					}).save(function(err, doc){
						if(err)
							res.json({"error": "data could not be created"})
						else {
							for(var i=0;i<req.body.people.length;i++){
								var person = req.body.people[i]
								User.findOne({ "email": person.email }, function(err, doc){
									if(err){
										res.json({"error": "user could not be found"})
										//break
									} else {
										doc.disease = req.body.disease,
										doc.severity = 3
										doc.save(function(err, doc){
											if(err){
												res.json({"error": "user could not be updated"})
												//break
											} else {
												console.log(doc)
												Data({ 
													lat: person.lat,
													long: person.long,
													user_email: person.email,
													disease: req.body.disease,
													severity: 3
												}).save(function(err, doc){
													if(err){
														res.json({"error": "data could not be created"})
														//break
													} else {
														console.log(doc)
													}
												})
											}

										})
									}
								})
							}
							res.json({"message": "all good"})
						}
					})
				})
			})
		}
	})
})

// doctor only function
app.post('/update', function(req, res){
	// input email
	Data.find({ "user_email": req.body.email}, function(err, datas){
		Data.find({}, function(err, docs){
			var foo = []
			for(var i=0; i < docs.length; i++){
				var bar = docs[i]
				for(var n=0; i < datas.length; i++){
					var doc = datas[n]
					if((-0.0002 < doc.lat - bar.lat < 0.0002) && (-0.0002 < doc.long - bar.long < 0.0002) && (10 < Date(doc.createdAt()) - Date(bar.createdAt()) < 10)){
						foo.push(doc)
						doc.disease = req.body.disease // "predictive"
						doc.severity = 3
						doc.save()
						Data.find({ user_email: doc.user_email}, function(err, data){
							for(var i=0;i<data.length;i++){
								data[i].severity = 3
								data[i].disease = req.body.disease
								data[i].save()
							}
						})
					}
				}
			}
			res.json(foo) // next nodes that connect
		})
	})
	// input [{ time: ..., lat: ... , long: ... }, { ... }, ...]
})

app.get('/some_data/:disease', function(req, res){
	Data.find({ disease: req.params.disease }, function(err, datas){
		res.json(datas)
	})
})


app.get('/some_data', function(req, res){
	Data.find({ disease: { $ne: null } }, function(err, datas){
		res.json(datas)
	})
})

app.get('/get_person/:email', function(req, res){
	User.findOne({email: req.params.email }, function(err, doc){
		if(err)
			res.json({"error": "no user found"})
		else {
			if(doc == undefined){ // == cuz check for both null and undefined
				res.json({"error": "no user found"})	
			} else {
				res.json(doc)
			}
		}
	})
})

app.post('/create_person', function(req, res){
	var user = User({
		email: req.body.email,
		disease: null,
		severity: 0,
		doctor: req.body.doctor,
		password: null
	})
	user.save(function(err, user){
		if(err)
			res.json({"error": "user could not be created"})
		else
			res.json(user)
	})
})

app.get('/data', function(req, res){
	Data.find({}, function(err, doc){
		if(err)
			res.json({"error": "failed to get data"})
		else if(doc == undefined)
			res.json({"error": "failed to get data"})
		else
			res.json(doc)
	})
})

app.post('/data', function(req, res){
	var data = Data({
		lat: req.body.lat,
		long: req.body.long,
		user_email: req.body.user_email,
		disease: null,
		severity: 0
	})
	data.save(function(err, data){
		if(err)
			res.json({"error": "error"})
		else
			res.json(data)
	})
})

app.post('/geodata', function(req, res){
	var lat = req.body.lat 
	var long = req.body.long
	var email = req.body.email
	GeoData({
		lat: lat,
		long: long,
		email: email
	}).save(function(err, geoData){
		if(err)
			res.json({"error": "geodata could not be saved"})
		else
			res.json(geoData)
	})
})

app.get('/geodata', function(req, res){
	GeoData.find({}, function(err, docs){
		if(err)
			res.json({"error": "could not find data information"})
		else if(docs == undefined)
			res.json({"error": "could not find data information"})
		else
			res.json(docs)
	})
})

app.get('/geodata/:email', function(req, res){
	var email = req.params.email
	GeoData.find({ email: email }, function(err, docs){
		if(err)
			res.json({"error": "could not find data information"})
		else if(docs == undefined){
			res.json({"error": "could not find data information"})
		} else {
			res.json(docs)
		}
	})
})

app.listen(3000, function(){
	console.log("server running on port 3000")
})