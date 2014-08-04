package com.blockchain

import java.io.FileInputStream
import org.apache.commons.codec.binary.Hex
import java.io.ObjectInputStream
import java.io.DataInputStream
import java.io.ByteArrayInputStream

class Script(data: Array[Byte]) {
  if (data(0) == 118 && data(1) == -87) { // OP_DUP OP_HASH160
    val hash: Array[Byte] = new Array(20)
    if (data.length == 25)
      Array.copy(data, 2, hash, 0, 20)
    else
      Console.println("Invalid script")
  }
  else if (data(data.length - 1) == -84)
  {
    
  }
  else 
    Console.println("Strange")
}

class Block(i: Int, data: Array[Byte]) {
  val dis = new DataInputStream(new ByteArrayInputStream(data))
  dis.readInt() // version
  /* Block header */
  val hashPrevBlock: Array[Byte] = new Array(32)
  dis.read(hashPrevBlock)
  dis.skipBytes(32)
  dis.skipBytes(4)
  dis.skipBytes(4)
  dis.readInt() // nonce
  
  val cTransactions = Block.readVI(dis)
  for (iTrans <- 0 until cTransactions) {
    dis.readInt()
    val cIn = Block.readVI(dis).toInt
    for (i <- 0 until cIn) {
      dis.skipBytes(32) // prev tx hash
      dis.skipBytes(4) // index
      val scriptLen = Block.readVI(dis)
      dis.skipBytes(scriptLen)
      dis.skipBytes(4) // seq no
    }
    val cOut = Block.readVI(dis).toInt
    for (i <- 0 until cOut) {
      val value = dis.skipBytes(8) // value
      val scriptLen = Block.readVI(dis)
      val scriptBytes: Array[Byte] = new Array(scriptLen)
      dis.read(scriptBytes)
      new Script(scriptBytes)
    }
    dis.skipBytes(4) // lock time
  }
}

object Block {
  def little2big(i: Int): Int = 
    ((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff)
  def little2big(i: Short): Short = 
    (((i&0xff)<<8)|((i&0xff00)>>8)).toShort
    
  def readVI(dis: DataInputStream): Int = {
    val b = dis.readByte().toInt & 0xFF
    b match {
      case 0xFD => little2big(dis.readShort())
      case 0xFE => little2big(dis.readInt())
      case 0xFF => throw new RuntimeException("Unsupported")
      case _ => b
    }
  }
    
  def main(args: Array[String]) {
    val fis = new FileInputStream("d:/bootstrap.dat")
    val ois = new DataInputStream(fis)
    var totalTransactions = 0L
    for (i <- 0 until 295000) {
      try {
      if (i == 100000)
        Console.print("")
      val magic = (ois.readInt())
      val size = little2big(ois.readInt())
      val buffer = new Array[Byte](size)
      val cnt = fis.read(buffer)
      val block = new Block(i, buffer)
      totalTransactions += block.cTransactions
      if (i % 1000 == 0)
        Console.println(i)
      }
      catch 
      {
        case e: Exception => 
          Console.println(s"Exception at block $i")
          throw e
      }
    }
    Console.println(s"Total # transactions = $totalTransactions")
  }
}