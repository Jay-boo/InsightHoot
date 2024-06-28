mod config;
mod redditClient;
mod me;
mod subreddit;
mod url;



use lazy_static::lazy_static;
use reqwest::{header::{USER_AGENT, HeaderValue}, Client, Response};
use redditClient::RedditClient;
use dotenv::dotenv;
use std::env;
use log::info;


lazy_static!(
    static ref USER_AGENT_NAME:String=env::var("USER_AGENT_NAME").expect("USER_AGENT_NAME not set");
    static ref CLIENT_ID:String=env::var("CLIENT_ID").expect("CLIENT_ID not set");
    static ref CLIENT_SECRET:String=env::var("CLIENT_SECRET").expect("CLIENT_SECRET not set");
    static ref USER_NAME:String=env::var("USER_NAME").expect("USER_NAME not set");
    static ref PASSWORD:String=env::var("PASSWORD").expect("PASSWORD not set");
);

#[tokio::main]
async fn main()-> Result<(),std::io::Error>  {
    env::set_var("RUST_LOG", "info");
    // env::set_var("RUST_BACKTRACE", "1");
    env_logger::init();
    dotenv().ok();
    info!("Authenticating to Reddit");

    let mut reddit_client:RedditClient=RedditClient::new(&*USER_AGENT_NAME, &*CLIENT_ID, &*CLIENT_SECRET);
    let me:me::me::Me=reddit_client.login(&USER_NAME, &PASSWORD).await.unwrap();
    let rfunny:subreddit::subreddit::Subreddit=me.get_subreddit("r/funny",Some(1),subreddit::feedoptions::FeedFilter::Hot).await;
    println!("-------------Feed :\n{:#?}",rfunny.feed);

    // -------------------------------------
    // Testing the connection

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


