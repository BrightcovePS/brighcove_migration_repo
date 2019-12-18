    //Insrtuction to use

    // Please install following dependancies
    //1.npm install request --save
    //2.npm install sync-request
    //3.npm install json2csv --save

    //Please update the client_id, client_secret and accoundId with respective one.

    var request = require('request');
    const client_id = "";
    const client_secret = "";
    const accoundId = "";
    var auth_string = new Buffer(client_id + ":" + client_secret).toString('base64');
    // console.log('auth_string: ', 'Basic ' + auth_string);
    var offset = 0;
    const limit = 100;
    var tokenExpiry = 0;
    var videoList = [];
    var accessToken;

    console.log('---------------------------Start Time-------------------- :- ',  new Date().getHours() + ":" +  new Date().getMinutes() + ":" +  new Date().getSeconds());

    getTokenWithExpiry();

    //Get video count
    request({ method: 'GET', url: 'https://cms.api.brightcove.com/v1/accounts/' + accoundId + '/counts/videos',
        headers: {
            'Authorization': accessToken,
            'Content-Type': 'application/json'
        },
        }, function (error, response, body) {
            if (error) {
                console.log('Error: ', error);
                return;
            }

            const jsnObj = JSON.parse(body);
            const count = jsnObj.count;
            
            console.log('--------------------------------Video Count On server---------------------------');
            console.log('Count: ', count);

            while (offset < count) {
                if((Date.now()/1000) < tokenExpiry){
                    console.log('--------------------------Index---------------------: ', offset);

                    var request = require('sync-request');
                    var res = request('GET', 'https://cms.api.brightcove.com/v1/accounts/' + accoundId + '/videos?limit=' + limit + '&offset=' + offset, {
                        headers: {
                            'Authorization': accessToken,
                            'Content-Type': 'application/json',
                        },
                    });
            
                    var videos = JSON.parse(res.getBody('utf8'))

                    for(var i= 0 ; i < videos.length; i++) {
                        videoList.push( videos[ i ] );
                    }
            
                    if (videos.length > 0) {
                        offset += limit;
                    }
                } else {
                    getTokenWithExpiry();
                }
        }

        console.log('--------------------------------VideoList Array Count---------------------------');
        console.log('Count: ', videoList.length);
        console.log('--------------------------------VideoList---------------------------');
        console.log('VideoList Array: ', videoList);

        const data = videoList;

        const { Parser } = require('json2csv');

        let fields = ["name", "id", "reference_id", "custom_fields.reserve_id",  "custom_fields.part_number", "custom_fields.supplier_id"];

        const parser = new Parser({
                           fields,
                           unwind: ["name", "id", "reference_id", "custom_fields.reserve_id",  "custom_fields.part_number", "custom_fields.supplier_id"]
                           });

        const csv = parser.parse(data);

        const fs = require('fs');
        fs.writeFile('Brightcove_' + accoundId + '.csv', csv, function(err) {
             if (err) {
                console.log('Error in file writing');
             } else {
                console.log('file saved');
            }
        });
            
        console.log('---------------------------FInish Time-------------------- :- ',  new Date().getHours() + ":" +  new Date().getMinutes() + ":" +  new Date().getSeconds());
    });

    // Get Auth Token
    function getTokenWithExpiry() {
        const request = require('sync-request');
        const res = request('POST', 'https://oauth.brightcove.com/v4/access_token',
                         { headers: {
                         'Authorization': 'Basic ' + auth_string,
                         'Content-Type': 'application/x-www-form-urlencoded'
                         },
                         body: 'grant_type=client_credentials'});

        const token = JSON.parse(res.getBody('utf8'))
        accessToken = 'Bearer ' +token.access_token
        tokenExpiry = Math.floor(Date.now()/1000) + 270
        console.log('New AccessToken:- ', token.access_token);
    }
