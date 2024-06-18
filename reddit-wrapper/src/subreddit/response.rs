use serde::Deserialize;

// #[derive(Deserialize,Debug)]
// pub struct SubredditResponse{
//     pub data:SubredditData,
//     kind:String
// }

#[derive(Deserialize,Debug)]
pub struct SubredditData{
    pub description:String
}


#[derive(Deserialize,Debug)]
pub struct FeedData<T>{
    children:Vec<T>
}

#[derive(Deserialize,Debug)]
pub struct PostData{
    pub pinned:bool,
    pub selftext:String,
    pub url:String,
    pub created_utc:f64
}


#[derive(Deserialize,Debug)]
pub struct BasicStruct<K,D>{
    kind:K,
    pub data:D
}


pub type FeedResponse=BasicStruct<String,FeedData<BasicStruct<String,PostData>>>;
