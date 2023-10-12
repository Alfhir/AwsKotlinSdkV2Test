import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.BucketLocationConstraint
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.sdk.kotlin.services.s3.model.DeleteBucketRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.minutes

const val REGION = "eu-central-1"
const val KEY = "SomeKeyValue"
val BUCKET = "bucket-${UUID.randomUUID()}"

fun main(args: Array<String>) = runBlocking {
  S3Client.fromEnvironment { region = REGION }
    .use { client -> client.run {
        setUpBucket()
        setupObject()
        writeObjectContentToFile()
        scanObjects()
        selfDestroy()
      }
    }
}

suspend fun S3Client.setUpBucket() {
  println("Creating bucket $BUCKET...")
  this.createBucket(CreateBucketRequest {
    bucket = BUCKET
    createBucketConfiguration {
      locationConstraint = BucketLocationConstraint.fromValue(REGION)
    }
  })
  println("Created bucket $BUCKET successfully!")
}

suspend fun S3Client.cleanupBucket() {
  println("Deleting bucket $BUCKET...")
  this.deleteBucket(DeleteBucketRequest {
    bucket = BUCKET
  })
  println("Deleted bucket $BUCKET successfully!")
}

suspend fun S3Client.setupObject() {
  this.putObject(PutObjectRequest {
    bucket = BUCKET
    key = KEY
    metadata = mapOf("myVal" to "test")
    body = ByteStream.fromString("Testing the Kotlin SDK v2")
  })
}

suspend fun S3Client.cleanupObject() {
  println("Deleting object $BUCKET/$KEY...")
  this.deleteObject(DeleteObjectRequest {
    bucket = BUCKET
    key = KEY
  })
  println("Object $BUCKET/$KEY deleted successfully!")
}

suspend fun S3Client.cleanUp() {
  cleanupObject()
  cleanupBucket()
}

suspend fun S3Client.writeObjectContentToFile() {
  this.getObject(GetObjectRequest {
    key = KEY
    bucket = BUCKET
  }) { resp ->
    val myFile = File("./test.txt")
    resp.body?.writeToFile(myFile)
    println("Successfully read $KEY from $BUCKET")
  }
}

suspend fun S3Client.scanObjects() {
  println("Scanning objects in bucket $BUCKET...")
  listObjects(ListObjectsRequest {
    bucket = BUCKET
  }).contents?.forEachIndexed { index, it ->
    it.run {
      println("Object $index")
      println("Owner = $owner")
      println("Key = $key")
      println("Size = $size")
      println("Storage class = $storageClass")
    }
  }
}

suspend fun S3Client.selfDestroy() {
  println("In one minute this bucket will self-destroy...")
  delay(1.minutes)
  cleanUp()
}
