mod config;
mod redditClient;
mod me;
mod subreddit;
mod url;



use std::fmt::Write;
use futures::StreamExt;
use kafka::producer::{self, Producer, AsBytes};
use lazy_static::lazy_static;
use reqwest::{header::{USER_AGENT, HeaderValue}, Client, Response};
use redditClient::RedditClient;
use dotenv::dotenv;
use std::{env, time::Duration, collections::HashSet };
use log::info;
use crate::subreddit::response::PostData;


lazy_static!(
    static ref USER_AGENT_NAME:String=env::var("USER_AGENT_NAME").expect("USER_AGENT_NAME not set");
    static ref CLIENT_ID:String=env::var("CLIENT_ID").expect("CLIENT_ID not set");
    static ref CLIENT_SECRET:String=env::var("CLIENT_SECRET").expect("CLIENT_SECRET not set");
    static ref USER_NAME:String=env::var("USER_NAME").expect("USER_NAME not set");
    static ref PASSWORD:String=env::var("PASSWORD").expect("PASSWORD not set");
    static ref KAFKA_HOST:String=env::var("KAFKA_HOST").expect("KAFKA_HOST not set");
);

#[tokio::main]
async fn main()-> Result<(),std::io::Error> {
    env::set_var("RUST_LOG", "debug");
    // env::set_var("RUST_BACKTRACE", "1");
    env_logger::init();
    dotenv().ok();
    info!("Authenticating to Reddit");
    let mut reddit_client:RedditClient=RedditClient::new(&*USER_AGENT_NAME, &*CLIENT_ID, &*CLIENT_SECRET);
    let me:me::me::Me=reddit_client.login(&USER_NAME, &PASSWORD).await.unwrap();
    info!("{}",format!("Get subreddit: {} ","r/funny"));
    let rfunny:subreddit::subreddit::Subreddit=me.get_subreddit("r/funny",Some(1),subreddit::feedoptions::FeedFilter::Hot).await;
    let (mut stream_posts,join_handle)= rfunny.stream_items(Duration::new(10, 0),"Nothing".to_string(),None);

    let mut seen_posts:HashSet<String>= HashSet::new();
    let mut producer=Producer::from_hosts(
        vec!(KAFKA_HOST.to_string())
        )
        .with_ack_timeout(Duration::from_secs(1))
        .with_required_acks(producer::RequiredAcks::One)
        .create()
        .unwrap();
    let mut buf=String::with_capacity( 2);


    

    while let  Some(posts)=stream_posts.next().await{
        let posts= match posts {
            Ok(p) => p,
            Err(err) => {
                log::error!("{}",err);
                continue;
            }
                ,
        };
        for post in posts.data.children{
            let post_data:PostData=post.data;
            if seen_posts.contains(&post_data.permalink){
                log::debug!("Duplicate post skipped : {}",&post_data.permalink);
                continue;
            }
            seen_posts.insert(post_data.permalink);
            log::info!("New post fetch from {}",&rfunny.name);
            producer.send(&producer::Record::from_value("r-funny-reddit", post_data.selftext.as_bytes()) );

        };
    }


    Ok(())

}

#[cfg(test)]
mod tests{

    use core::panic;
    use dotenv::dotenv;
    use reqwest::{Client, Response, header::USER_AGENT};

    lazy_static::lazy_static!{
        static ref USER_AGENT_NAME:String=std::env::var("USER_AGENT_NAME").expect("USER_AGENT_NAME not set");
        static ref CLIENT_ID:String=std::env::var("CLIENT_ID").expect("CLIENT_ID not set");
        static ref CLIENT_SECRET:String=std::env::var("CLIENT_SECRET").expect("CLIENT_SECRET not set");
        static ref USER_NAME:String=std::env::var("USER_NAME").expect("USER_NAME not set");
        static ref PASSWORD:String=std::env::var("PASSWORD").expect("PASSWORD not set");
    }

    #[tokio::test]
    async fn test_auhentication(){
        println!("Authentication test");
        dotenv().ok();
        let url:&str="https://www.reddit.com/api/v1/access_token";
        let form = [
                ("grant_type", "password"),
                (
                    "username",&USER_NAME
                ),
                (
                    "password",&PASSWORD
                ),
            ];
        let response:Response=match Client::new()
            .post(url).header(USER_AGENT,&*USER_AGENT_NAME)
            .basic_auth(&*CLIENT_ID,Some(&*CLIENT_SECRET)).form(&form).send().await{
                Ok(response)=>response,
                Err(_e)=> panic!("{}",format!("Authentication request failed to {}  \nwith{:#?}!",url,form))
            };
        
        println!("response:{:#?}",response.status())
    }
}


