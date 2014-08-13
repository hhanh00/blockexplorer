package com.blockchain

import java.io.FileInputStream
import org.apache.commons.codec.binary.Hex
import java.io.ObjectInputStream
import java.io.DataInputStream
import java.io.ByteArrayInputStream
import java.security.Security
import java.security.MessageDigest
import de.flexiprovider.core.FlexiCoreProvider
import scala.collection.mutable.HashMap
import java.nio.ByteBuffer
import org.apache.commons.lang3.ArrayUtils
import hanh.com._

class Script(data: Array[Byte]) {
  if (data(0) == 118 && data(1) == -87) { // OP_DUP OP_HASH160
    val hash: Array[Byte] = new Array(20)
    if (data.length == 25)
      Array.copy(data, 2, hash, 0, 20)
    else
      Console.println("Invalid script")
  } else if (data(data.length - 1) == -84) {

  } else
    Console.println("Strange")
}

case class Transaction(hash: String, outputs: Array[Long]) {
  var cUnspent = outputs.length
  def decUnspent() = 
    cUnspent -= 1
}

object Transaction {
  def apply(hash: Array[Byte], outputs: Array[Long]) = {
    /*
    val hcopy = hash.clone()
    ArrayUtils.reverse(hcopy)
    new Transaction(new String(Hex.encodeHex(hcopy)), outputs)
    */
    new Transaction("", outputs)
  }
}

class BlockChain(val txs: HashMap[ByteBuffer, Transaction]) {

}

object BlockChain {
  def apply() = new BlockChain(HashMap.empty)
}

class Block(bchain: BlockChain, i: Int, data: Array[Byte]) {
  val dis = new DataInputStream(new ByteArrayInputStream(data))
  dis.readInt() // version
  /* Block header */
  val hashPrevBlock: Array[Byte] = new Array(32)
  dis.read(hashPrevBlock)
  dis.skipBytes(32)
  dis.skipBytes(4)
  dis.skipBytes(4)
  dis.readInt() // nonce
  
  val m = Mess(1)

  val md = MessageDigest.getInstance("SHA256", "FlexiCore")
  val cTransactions = Block.readVI(dis)
  for (iTrans <- 0 until cTransactions) {
    val avail = dis.available()
    dis.readInt()
    val cIn = Block.readVI(dis).toInt
    for (i <- 0 until cIn) {
      val prevHash: Array[Byte] = new Array(32)
      dis.read(prevHash)
      val index = Block.little2big(dis.readInt()) // index
      val scriptLen = Block.readVI(dis)
      dis.skipBytes(scriptLen)
      dis.skipBytes(4) // seq no
      if (iTrans != 0) {
        val prevTxH = ByteBuffer.wrap(prevHash)
        val prevTx = bchain.txs(prevTxH)
        prevTx.outputs(index) = 0
        prevTx.decUnspent()
        if (prevTx.cUnspent == 0)
          bchain.txs.remove(prevTxH)
      }
    }
    val cOut = Block.readVI(dis).toInt
    val txOutputs: Array[Long] = new Array(cOut)
    for(i <- 0 until cOut) {
      val value = java.lang.Long.reverseBytes(dis.readLong()) // value
      val scriptLen = Block.readVI(dis)
      dis.skipBytes(scriptLen)
      txOutputs(i) = value
    }
    dis.skipBytes(4) // lock time
    val avail2 = dis.available()
    val txLen = avail - avail2
    md.update(data, data.length - avail, txLen)
    val txHash0 = md.digest()
    md.update(txHash0)
    val txHash = md.digest()
    bchain.txs.put(ByteBuffer.wrap(txHash), Transaction(txHash, txOutputs))
  }
}

object Block {
  def little2big(i: Int): Int =
    ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8) + ((i >> 24) & 0xff)
  def little2big(i: Short): Short =
    (((i & 0xff) << 8) | ((i & 0xff00) >> 8)).toShort

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
    Security.addProvider(new FlexiCoreProvider())
    val bchain = BlockChain()

    val fis = new FileInputStream("/Users/hanhhuynhhuu/Downloads/bootstrap.dat")
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
        val block = new Block(bchain, i, buffer)
        totalTransactions += block.cTransactions
        if (i % 10000 == 0) {
          Console.println(i)
          Console.println(s"TXs = ${bchain.txs.size}")
        }
      } catch {
        case e: Exception =>
          Console.println(s"Exception at block $i")
          throw e
      }
    }
    Console.println(s"Total # transactions = $totalTransactions")
  }
}