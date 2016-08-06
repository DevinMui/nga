# NGA

## Server

### Paths

`POST /update_person` - Params: array. Ex: { "user": { ... doctor details including password },"email": ... , "lat": ... , "long": ... , "disease": ... , [ {"email": ... , "lat": ... , "long": ... }, {}, {} ] }

`GET /get_person` - Params: email

`POST /create_person` - Params: email

Gzip compressed