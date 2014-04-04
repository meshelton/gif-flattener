package com.sheltonsys

import javax.imageio._
import java.io.File
import java.awt.image.BufferedImage

object Main {

    def main(args: Array[String]) = {
        println("Welcome to gif-flattener")

        lazy val gifFile = new File(args(0))
        if(args.length == 0 || (!gifFile.exists() || gifFile.isDirectory())){
            println("Please select a gif file and try again, now exiting")
            System.exit(1)
        }

        println(s"Attempting to flatten $gifFile")
        val imageReader = ImageIO.getImageReadersByFormatName("gif").next()
        val inputStream = ImageIO.createImageInputStream( gifFile )
        imageReader.setInput(inputStream, false, false)

        val numFrames = imageReader.getNumImages(true)
        println(s"This gif has $numFrames frames")
    }
}
