use std::{i64, time::Duration, fmt::{Debug, Display, Pointer}  };
use futures::{Stream, channel::mpsc, Sink, TryFutureExt};
use reqwest::{Client, Response};
use tokio::{task::JoinHandle, time::error::Elapsed };
use log;

use crate::{url::buildUrl, subreddit::response::{BasicStruct,SubredditData, FeedResponse},subreddit::stream_error::StreamError};
use crate::subreddit::feedoptions::{FeedSort,FeedFilter};




#[derive(Clone)]
pub struct Subreddit{
    pub name:String,
    pub about:Option<String>,
    pub feed:Option<FeedResponse>,
    client: Client
}

impl Subreddit{
    pub fn new(name:&str,client:&Client)-> Subreddit{
        Subreddit{
            name:name.to_string(),
            about:None,
            feed:None,
            client:client.to_owned()
        }
    }

    pub async fn get_about(&mut self)-> Result<(),std::io::Error>{
        let dest:&str=&format!("{}/about",self.name).to_string();
        let url:&str=&buildUrl(dest).to_string();
        let response:Response= match self.client.get(url).send( ).await{
            Ok(value)=> value,
            Err(_e)=> return Err(std::io::Error::new(std::io::ErrorKind::NotFound,format!("Not found url : {}",url)))
        };
        let subreddit_resp:BasicStruct<String, SubredditData>=match response.json::<BasicStruct<String,SubredditData>>().await{
            Ok(value)=> value,
            Err(_e)=> return Err(std::io::Error::new(std::io::ErrorKind::NotFound,format!("Not found url : {}",url)))

        };
        println!("Subreddit about response : \n{:#?}",subreddit_resp);
        self.about=Some(subreddit_resp.data.description);
        Ok(())
    }

    async fn get_feed(&mut self,feedOption:FeedFilter,limit:Option<i64>,feedSort:Option<FeedSort>)->Result<(),std::io::Error>{
        let limit_string:String=match limit{
            Some(limit)=>format!("limit={}",limit),
            None=>String::from("")
        };
        let sort_option_string:String=match feedSort{
            Some(sort_option)=>format!("sort={}",sort_option.as_str()),
            None=> String::from("")
        };

        let dest:&str=&format!("{}/{}/.json?{}&{}",self.name,feedOption.as_str(),limit_string,sort_option_string.as_str()).to_string();
        let url:&str=&buildUrl(dest).to_string();
        println!("Feed url :{}",url);
        let response:Response= match self.client.get(url).send( ).await{
            Ok(value)=> value,
            Err(_e)=> return Err(std::io::Error::new(std::io::ErrorKind::NotFound,format!("Not found url : {}",url)))
        };
        let feed_data:FeedResponse=response.json::<FeedResponse>().await.unwrap();
        self.feed=Some(feed_data);
        // NEED TO REMOVE PIN OR ADMIN POST
        Ok(())
    }
    
    
    
    async fn send_message<S:Sink<String>>(&mut self,sleep_time:Duration,retry_strategy:String,timeout:Option<Duration>,sender:S)->Result<(),mpsc::SendError>{
        // return mpsc::SendError when there is an error sending msg to receiver
        
        loop{

            log::info!("Fetching latest submission from source");
            if let Some(timeout_duration)= timeout{
                let timeout_obj:Result<Result<(), std::io::Error>, tokio::time::error::Elapsed>=tokio::time::timeout(
                    timeout_duration,
                    self.get_feed(FeedFilter::New,
                        None,
                        Some(FeedSort::Latest)
                    ) 
                ).await;


                match timeout_obj{
                    Err(err)=>Err(StreamError::TimeoutError(err)),
                    Ok(val)=>match val{
                        Ok(fetch_status)=>Ok(fetch_status),
                        Err(fetch_err)=>Err(StreamError::SourceError(fetch_err))
                    }
                }

            }else {
                match self.get_feed(
                    FeedFilter::New,
                    None,
                    Some(FeedSort::Latest)
                ).await{
                        Ok(val)=>Ok(val),
                        Err(err)=>Err(StreamError::SourceError(err))

                }

                
            };
            break;

        }


        Ok(())


    }
    fn stream_items(&self,sleep_time:Duration,retry_strategy:String,timeout:Option<Duration>)->(impl Stream<Item=String>,JoinHandle<Result<(),mpsc::SendError>>){
        let (sender,receiver)=mpsc::unbounded();
        let mut owned_subreddit=self.clone();
        let fetch_post_task:JoinHandle<Result<(),mpsc::SendError>>=tokio::task::spawn(async move{
            owned_subreddit.send_message(sleep_time, retry_strategy, timeout,sender).await
        });
        (receiver,fetch_post_task)

    }
    // fn stream_subreddit_post(&self,sleep_time:Duration,retry_strategy:String,timeout:Option<Duration>)-> impl Stream<Item=String>{
    //
    // }

}

#[cfg(test)]
mod tests{
    use std::thread::JoinHandle;
    use std::time::Duration;

    use dotenv::dotenv;
    use futures::Stream;
    use futures::channel::mpsc;
    use crate::redditClient::RedditClient;
    use crate::me;
    use crate::subreddit;


    lazy_static::lazy_static!{
        static ref USER_AGENT_NAME:String=std::env::var("USER_AGENT_NAME").expect("USER_AGENT_NAME not set");
        static ref CLIENT_ID:String=std::env::var("CLIENT_ID").expect("CLIENT_ID not set");
        static ref CLIENT_SECRET:String=std::env::var("CLIENT_SECRET").expect("CLIENT_SECRET not set");
        static ref USER_NAME:String=std::env::var("USER_NAME").expect("USER_NAME not set");
        static ref PASSWORD:String=std::env::var("PASSWORD").expect("PASSWORD not set");
    }



    #[tokio::test]
    async fn test_stream_subreddit_post(){
        dotenv().ok();
        let mut reddit_client:RedditClient=RedditClient::new(&*USER_AGENT_NAME, &*CLIENT_ID, &*CLIENT_SECRET);
        let me:me::me::Me=reddit_client.login(&USER_NAME, &PASSWORD).await.unwrap();
        let rfunny:subreddit::subreddit::Subreddit=me.get_subreddit("r/funny",Some(1),subreddit::subreddit::FeedFilter::Hot).await;

        let (stream,join_handle)=rfunny.stream_items(Duration::new(30, 0), "Nothing".to_string(), None);
    }
}



