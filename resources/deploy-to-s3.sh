echo "Deploy to s3 [y/n]?"
read -r -p "" response
case "$response" in
    [yY][eE][sS]|[yY]) 
        aws s3 mv target/TouchGrassJavaLambda.jar s3://touchgrasscdkstack-touchgrassjavalambdabucketdbf5-137vkpnaei6hw && echo 's3://touchgrasscdkstack-touchgrassjavalambdabucketdbf5-137vkpnaei6hw/TouchGrassJavaLambda.jar'
        ;;
    *)
        echo Not deployed.
        ;;
esac
