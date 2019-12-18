/// install:-  npm install ooyala-api

const assetId = ''; // embedde code
const api_key = '';
const api_secret = '';

const OoyalaApi = require('ooyala-api');
const api = new OoyalaApi(api_key, api_secret, {concurrency: 10});

console.log('---------------------------Start Time--------------------  ', assetId + ":- "  + new Date().getHours() + ":" +  new Date().getMinutes() + ":" +  new Date().getSeconds());

api.get('/v2/assets/' + assetId + '/closed_captions')
.then((items) => {
        const xml = items.toString('utf8');
        const fs = require('fs')

        fs.writeFile('Ooyala_CC_' + assetId + '.xml', xml, function(err) {
            if (err) {
                console.log('Error in file writing');
            } else {
                console.log('file saved');
            }
        });

        console.log('---------------------------FInish Time--------------------  ', assetId + ":- " +  new Date().getHours() + ":" +  new Date().getMinutes() + ":" +  new Date().getSeconds());
    })
.catch((err) => {
       console.log('Error: ', err);
       });
