# Encode your Azure connection string
echo -n "DefaultEndpointsProtocol=https;AccountName=teamsyncv1;AccountKey=Ur+poMBza4Mb0W515fSlbX42kYW6TexCzXd6ooO3/QcN8HnfDu+U6pzGJhkpjUVQDSP5UhrifzPx+AStTDdreA==;EndpointSuffix=core.windows.net" | base64

# Encode your container name
echo -n "teamsync-files" | base64

# Encode your account name
echo -n "teamsyncv1" | base64

# Encode your SAS token
echo -n "sp=racwd&st=2025-07-28T09:51:31Z&se=2025-08-09T18:06:31Z&sip=0.0.0.0&sv=2024-11-04&sr=c&sig=yUmF%2BjYZkTI5uVYeLglLmq48ddV5oKns8qXVWrCtObs%3D" | base64