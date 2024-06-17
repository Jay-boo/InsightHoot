use std::io;

use reqwest::header::{HeaderMap, AUTHORIZATION, USER_AGENT,HeaderValue};
use reqwest::{Response, Client};
use crate::url::buildUrl;

use crate::config::{Config };
pub struct Me{
    config:Config,
    client:reqwest::Client
}

impl Me{
    pub fn new(config:&Config,client:&reqwest::Client)->Me{
        Me{
            config:config.to_owned(),
            client:client.to_owned()
        }
    }

    async fn get(&self,dest:&str)->Result<Response,io::Error>{
        let built_url:String=buildUrl(&dest.to_string());
        println!("Aimed Url: {}",built_url);
        let response:Response= match self.client.get(&built_url).send().await{
            Ok(response)=>response,
            Err(_e)=> return Err(io::Error::new(io::ErrorKind::NotConnected,"Not valid  url request "))
        };


        if response.status()==200 {
            println!("Success");
        }
        else if response.status()==403 {
            println!("Header format problem met");
            let mut headers=HeaderMap::new();
            headers.insert(USER_AGENT, HeaderValue::from_str(&self.config.user_agent).unwrap());
            let resp:Response=match Client::builder().default_headers(headers).build().unwrap().get(&built_url).send().await{
                Ok(response)=>response,
                Err(_e)=> return Err(io::Error::new(io::ErrorKind::NotConnected,"Not valid  url request "))
            };
            if resp.status()==200{
                return Ok(resp);
            }else{
                return Err(io::Error::new(io::ErrorKind::NotConnected,format!("Authentication request failed ! Watching  response status : {}",resp.status())));
            }
        }
        else{
            return Err(io::Error::new(io::ErrorKind::NotConnected,format!("Authentication request failed ! Watching  response status : {}",response.status())));
        }
        Ok(response)
    }

    pub async fn search_subreddit(&self,query:&str,limit:i32){
        let url:&str=&buildUrl(&format!("subreddits/search.json?q={}&limit={}",&query,&limit).to_string());
        let response:Response=self.get(url).await.unwrap( );

    }
}
