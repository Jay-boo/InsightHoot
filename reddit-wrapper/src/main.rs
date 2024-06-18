mod config;
mod redditClient;
mod me;
mod subreddit;

mod url;

use std::io ;
use lazy_static::lazy_static;
use reqwest::{header::{USER_AGENT, HeaderValue}, Client, Response};
use redditClient::RedditClient;
use dotenv::dotenv;
use serde::de::value;
use std::env;
use roux::Reddit;



lazy_static!(
    static ref USER_AGENT_NAME:String=env::var("USER_AGENT_NAME").expect("USER_AGENT_NAME not set");
    static ref CLIENT_ID:String=env::var("CLIENT_ID").expect("CLIENT_ID not set");
    static ref CLIENT_SECRET:String=env::var("CLIENT_SECRET").expect("CLIENT_SECRET not set");
    static ref USER_NAME:String=env::var("USER_NAME").expect("USER_NAME not set");
    static ref PASSWORD:String=env::var("PASSWORD").expect("PASSWORD not set");
);

#[tokio::main]
async fn main()-> Result<(),std::io::Error>  {
    // env::set_var("RUST_BACKTRACE", "1");
    dotenv().ok();
    println!("Authenticating to Reddit");

    let mut reddit_client:RedditClient=RedditClient::new(&*USER_AGENT_NAME, &*CLIENT_ID, &*CLIENT_SECRET);
    let me:me::me::Me=reddit_client.login(&USER_NAME, &PASSWORD).await.unwrap();
    me.get_subreddit("r/funny",Some(1),subreddit::subreddit::FeedFilter::Hot).await;

    // let response:Response=me.get("r/funny/top").await.unwrap();
    // println!("{:#?}",response.text().await);

    // let client = Reddit::new("myuseragent","NCwsEETG7QrG4KRKHXY5Bw","dLDSfZ1JFQHIxibSjgTrtQRiUWfC8w")
    //     .username("RiceDelicious4164")
    //     .password("7grM3Cp5mdAfGWbF")
    //     .login()
    //     .await;
    // match client{
    //     Ok(val)=>{
    //         if let Some(access_token)=val.config.access_token{
    //             println!("Sucesss:{}",access_token)
    //
    //         }else{println!("No access token found")}
    //     },
    //     Err(e)=>println!("error")
    // }

    Ok(())

}

