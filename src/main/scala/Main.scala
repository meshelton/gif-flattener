package com.sheltonsys

import javax.imageio._
import java.io.File
import java.awt.image.BufferedImage
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.util.Timeout
import akka.pattern.ask
import akka.dispatch.ExecutionContexts._
import akka.actor.ActorSystem
import akka.actor.Props

object Main {

    implicit val ec = global

    def main(args: Array[String]) = {
        println("Welcome to gif-flattener")

        lazy val gifFile = new File(args(0))
        if(args.length == 0 || (!gifFile.exists() || gifFile.isDirectory())){
            println("Please select a gif file and try again, now exiting")
            System.exit(1)
        }
        println(s"Attempting to flatten $gifFile")

        val system = ActorSystem("System")
        implicit val timeout = Timeout(5 seconds)
        val actor = system.actorOf(Props(classOf[ImageSeperatorActor], gifFile)) 
        val future = actor ? StartProcessFileMsg()
        try {
            Await.result(future, timeout.duration).asInstanceOf[Any]  match {
                case i: BufferedImage => {
                    println("Writing image")
                    ImageIO.write(i, "jpeg", new File(args(0).stripSuffix(".gif") + ".jpeg"))
                    println("Finished writing image")
                }
                case _ => println("The wrong thing was returned")
            }            
        } catch {
            case e: Exception =>
                println("ERROR: " + e) // TODO: handle exception\n}
        } finally {
            system.shutdown
            println("Thanks for using gif-flattener!")
            System.exit(0)
        }
    }
}
