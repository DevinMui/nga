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
	email: { type: String, unique: true},
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

app.get('/doctor', function(req, res){
	res.render('doctor')
})

app.get('/memes', function(req, res){
	res.redirect("https://www.youtube.com/watch?v=HgQEuPw942c")
})

// doctor only function
app.post('/update', function(req, res){
	// input email
	Data.find({ "user_email": req.body.email}, function(err, datas){
		//console.log(datas)
		if(err) console.log(err)
		for(var i=0; i<datas.length;i++){
			var data = datas[i]
			data.disease = req.body.disease
			data.severity = 4
			data.save(function(err,doc){
				if(err) console.log(err)
				//console.log(doc)
			})
		}

		Data.find({ user_email: { $ne: req.body.email } }, function(err, docs){
			var foo = []
			for(var i=0; i < docs.length; i++){
				var bar = docs[i]
				//console.log(bar)
				for(var n=0; n < datas.length; n++){
					var doc = datas[n]
					//console.log(doc)
					//res.send(Date(doc.createdAt) - Date(bar.createdAt))
					var docDate = (+new Date(doc.createdAt)) 
					var barDate = (+new Date(bar.createdAt)) 
					//console.log(String((docDate - barDate) / 1000))
					//console.log(doc.createdAt)
					if((-0.00002 < doc.lat - bar.lat < 0.00002) && (-0.00002 < doc.long - bar.long < 0.00002) && (-100 < docDate - barDate < 100)){
						foo.push(bar)
						bar.disease = req.body.disease // "predictive"
						bar.severity = 3
						bar.save()
						Data.find({ user_email: bar.user_email}, function(err, data){
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
	Data.find({ severity: 4 }, function(err, doc){
		if(err)
			res.json({"error": "failed to get data"})
		else if(doc == undefined)
			res.json({"error": "failed to get data"})
		else
			res.json(doc)
	})
})

app.get('/less_severe', function(req, res){
	Data.find({ severity: 3}, function(err, doc){
		if(err)
			res.json({"error": "failed to get data"})
		else if(doc == undefined)
			res.json({"error": "failed to get data"})
		else
			res.json(doc)
	})
})

app.get('/alldata', function(req, res){
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