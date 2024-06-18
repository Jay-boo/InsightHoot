use std::i64;

use reqwest::{Client, Response};

use crate::url::buildUrl;
use super::response:: SubredditResponse;

pub enum FeedFilter{
    Hot,
    Top,
    New,
    Random,
    Rising,
    Controversial
}



impl FeedFilter{
    fn as_str(&self)->&str{
        match self{
            FeedFilter::Hot=>"hot",
            FeedFilter::Controversial=>"controversial",
            FeedFilter::Random=>"random",
            FeedFilter::New=>"new",
            FeedFilter::Top=>"top",
            FeedFilter::Rising=>"rising"
        }

    }
}

pub struct Subreddit{
    name:String,
    about:Option<String>,
    client: Client
}

impl Subreddit{
    pub fn new(name:&str,client:&Client)-> Subreddit{
        Subreddit{
            name:name.to_string(),
            about:None,
            client:client.to_owned()
        }
    }

    async fn get_about(&mut self)-> Result<(),std::io::Error>{
        let dest:&str=&format!("{}/about",self.name).to_string();
        let url:&str=&buildUrl(dest).to_string();
        let response:Response= match self.client.get(url).send( ).await{
            Ok(value)=> value,
            Err(_e)=> return Err(std::io::Error::new(std::io::ErrorKind::NotFound,format!("Not found url : {}",url)))
        };
        let subreddit_resp:SubredditResponse=match response.json::<SubredditResponse>().await{
            Ok(value)=> value,
            Err(_e)=> return Err(std::io::Error::new(std::io::ErrorKind::NotFound,format!("Not found url : {}",url)))

        };
        println!("Subreddit about response : \n{:#?}",subreddit_resp);
        self.about=Some(subreddit_resp.data.description);
        Ok(())

    }

    async fn get_feed(&mut self,sortMethod:FeedFilter,limit:Option<i64>)->Result<(),std::io::Error>{
        println!("In getFeed()");
        let limit_string:String=match limit{
            Some(limit)=>format!("limit={}",limit),
            None=>String::from("")
        };

        let dest:&str=&format!("{}/{}/.json?{}",self.name,sortMethod.as_str(),limit_string).to_string();
        let url:&str=&buildUrl(dest).to_string();
        println!("Feed url :{}",url);
        let response:Response= match self.client.get(url).send( ).await{
            Ok(value)=> value,
            Err(_e)=> return Err(std::io::Error::new(std::io::ErrorKind::NotFound,format!("Not found url : {}",url)))
        };
        Ok(())
        // NEED TO REMOVE PIN OR ADMIN POST

        
    }

    pub async fn get_info(&mut self,feed_limit:Option<i64>,sortMethod:FeedFilter){
        let _=self.get_about().await;
        let _=self.get_feed(sortMethod,feed_limit).await;
    }
}

#[cfg(test)]
mod tests{
    #[test]
    fn test_add(){
        assert!(true);
    }
}



