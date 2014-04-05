package com.sheltonsys

import akka.actor.{Actor, ActorRef}
import akka.actor.Props
import akka.event.Logging
import javax.imageio._
import java.io.File
import java.awt.image.BufferedImage
import java.awt.Rectangle

case class FrameProcessed()
case class StartFrameProcessing(val image: BufferedImage, val index: Int)

class FrameProcessorActor(val finalImage: BufferedImage, val numFrames: Int) extends Actor {
    val log = Logging(context.system, this)
    private var running = false

    def receive = {
        case StartFrameProcessing(frame, index) => {
            if(!running){
                log.debug(s"Starting frame $index processing")
                println(s"processing frame $index")
                running = true
                val width = 10 //frame.getWidth / numFrames
                val height = frame.getHeight()
                val x = 15 //index * width
                val y = 0
                val rectangle = new Rectangle(x, y, width, height)
                val source = frame.getData(rectangle)
                val dest = finalImage.getRaster()

                //dest.setRect(source)

                sender ! FrameProcessed()
                println(s"Finished frame $index")
            } else {
                log.warning("StartFrameProcessing sent multiple times")
            }
        }
        case _ => println("Message not implemented")
    }

}
