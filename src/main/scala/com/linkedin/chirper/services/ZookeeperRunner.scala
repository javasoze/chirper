package com.linkedin.chirper.services
import org.apache.zookeeper.server.quorum.QuorumPeerMain

object ZookeeperRunner {
  def main(args: Array[String]) = {
	val params = new Array[String](1)
	params(0) = "config/zookeeper/zoo.cfg"
    QuorumPeerMain.main(params)    
  }
}
