package com.sheltonsys

import akka.actor.{Actor, ActorRef}
import akka.actor.Props
import akka.event.Logging
import javax.imageio._
import java.io.File
import java.awt.image.BufferedImage

case class StartProcessFileMsg()

class ImageSeperatorActor(val gifFile: File) extends Actor {
    val log = Logging(context.system, this)
    var finalImage: BufferedImage = null
    private var running = false
    private var fileSender: Option[ActorRef] = None
    private var totalFrames = 0
    private var processedFrames = 0

    def receive = {
        case StartProcessFileMsg() => {
            if(!running){
                log.debug("Starting gif processing")
                fileSender = Some(sender)
                val imageReader = ImageIO.getImageReadersByFormatName("gif").next()
                val inputStream = ImageIO.createImageInputStream( gifFile )
                imageReader.setInput(inputStream, false, false)
                totalFrames = imageReader.getNumImages(true)
                val frames = new Iterable[BufferedImage] {
                    // TODO: Don't use getNumImages
                    val numFrames = totalFrames
                    def iterator: Iterator[BufferedImage] = new Iterator[BufferedImage]{
                        var curFrame = 0;
                        def next(): BufferedImage = {
                            val image = imageReader.read(curFrame)
                            curFrame += 1
                            image
                        }
                        def hasNext: Boolean = curFrame < numFrames
                    }
                }
                val width = imageReader.getWidth(0)
                val height = imageReader.getHeight(0)
                val imageType = BufferedImage.TYPE_INT_ARGB
                finalImage = new BufferedImage(width, height, imageType)
                frames.zipWithIndex.foreach{ case (frame, index) =>
                    val prop = Props(classOf[FrameProcessorActor], finalImage, totalFrames)
                    val curActor = context.actorOf(prop)
                    curActor ! StartFrameProcessing(frame, index)
                }
                running = true
            } else {
                log.warning("StartProcessFileMsg sent multiple times")
            }
        }
        case FrameProcessed() => {
            processedFrames += 1
            if(totalFrames == processedFrames){
                fileSender match {
                    case Some(x) => x ! finalImage
                    case None => log.error("No filesender found...")
                }
            }
        }
        case _ => println("Message not implemented")
    }

}
