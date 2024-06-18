use serde::Deserialize;

#[derive(Deserialize,Debug)]
pub struct SubredditResponse{
    pub data:SubredditData,
    kind:String
}

#[derive(Deserialize,Debug)]
pub struct SubredditData{
    pub description:String
}
