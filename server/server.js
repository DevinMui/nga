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
			bcrypt.compare(req.body.user.password, doc.password, function(err, check) {
				if(err){
					res.json({"error": "weird error in bcrypt checking lel"})
				} else if(check){
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
				} else {
					res.json({"error": "incorrect credentials"})
				}
			})
		}
	})
})

app.get('/get_person', function(req, res){
	User.findOne({email: req.body.email }, function(err, doc){
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
	bcrypt.hash(req.body.password, saltRounds, function(err, hash) {
		var user = User({
			email: req.body.email,
			disease: null,
			severity: 0,
			doctor: req.body.doctor,
			password: hash
		})
		user.save(function(err, user){
			if(err)
				res.json({"error": "user could not be created"})
			else
				res.json(user)
		})
	})
})

app.listen(3000, function(){
	console.log("server running on port 3000")
})