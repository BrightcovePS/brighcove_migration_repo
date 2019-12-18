/// install:-  npm install ooyala-api
/// install:-  npm install json2csv --save
/// Update:- update api_key, api_secret and accoundId

const api_key = '';
const api_secret = '';
const accoundId = '';

const OoyalaApi = require('ooyala-api');
const api = new OoyalaApi(api_key, api_secret, {concurrency: 10});

console.log('---------------------------Start Time--------------------', accoundId + ":-"  + new Date().getHours() + ":" +  new Date().getMinutes() + ":" +  new Date().getSeconds());

// GET (with params + pagination)
api.get('/v2/assets', {where: ``}, {recursive: true})
.then((items) => {
      
      console.log('--------------------------------VideoList Array Count---------------------------');
      console.log('Count: ', items.length);
      
      const data = items;
      
      const { Parser } = require('json2csv');
      
      let fields = ["name", "embed_code", "asset_type", "created_at", "updated_at", "duration", "status", "original_file_name", "publishing_rule_id", "player_id"];
      
      const parser = new Parser({
                                fields,
                                unwind: ["name", "embed_code", "asset_type", "created_at", "updated_at", "duration", "status",  "original_file_name", "publishing_rule_id", "player_id"]
                                });
      
      const csv = parser.parse(data);
      
      const fs = require('fs');
      fs.writeFile('Ooyala_' + accoundId + '.csv', csv, function(err) {
                   if (err) {
                   console.log('Error in file writing');
                   } else {
                   console.log('file saved');
                   }
                   });
      console.log('---------------------------FInish Time--------------------', accoundId + ":-" +  new Date().getHours() + ":" +  new Date().getMinutes() + ":" +  new Date().getSeconds());
      
      })
.catch((err) => {
       console.log('Error: ', err);
       });
