//
//  ViewController.swift
//  test
//
//  Created by 谭钧豪 on 16/4/26.
//  Copyright © 2016年 谭钧豪. All rights reserved.
//

import Cocoa
import CFNetwork

var SERVER_TCP_PORT = 4700
var UDP_PORT: UInt16 = 58839
var SERVER_UDP_PORT = 7400
var IP = "127.0.0.1"

@_silgen_name("ipaddr_to_ulong") func ipaddr_to_ulong(str: UnsafePointer<CChar>) -> UInt32
@_silgen_name("sizeofint") func sizeofint(port: Int) -> Int

class ViewController: NSViewController, AsyncSocketDelegate, AsyncUdpSocketDelegate {
    
    var socket :AsyncSocket!
    var udpSocket :AsyncUdpSocket!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        udpSocket = AsyncUdpSocket(delegate: self)
        
        do{
            try udpSocket.bindToPort(UDP_PORT)
            try udpSocket.enableBroadcast(true)
        }catch{
            print(error)
        }
        udpSocket.receiveWithTimeout(-1, tag: 0)
        UDP_PORT = udpSocket.localPort()
        socket = AsyncSocket(delegate: self)
        
        let button = NSButton(frame: NSRect(x: 50, y: 50, width: 150, height: 200 ))
        button.title = "connect"
        button.action = #selector(self.connect)
        let sendbtn = NSButton(frame: NSRect(x: 200, y: 50, width: 150, height: 200 ))
        sendbtn.title = "sendmsg"
        sendbtn.action = #selector(self.sendmsg)
        self.view.addSubview(button)
        self.view.addSubview(sendbtn)
        // Do any additional setup after loading the view.
    }
    
    func sendmsg(){
        do{
            if !udpSocket.isConnected(){
                try udpSocket.connectToHost(IP, onPort: UInt16(SERVER_UDP_PORT))
            }
            
            udpSocket.sendData("Hello, This is from Mac.".dataUsingEncoding(NSUTF8StringEncoding), withTimeout: 5, tag: 1)
        }catch{
            print(error)
        }
        
    }
    
    func onUdpSocket(sock: AsyncUdpSocket!, didReceiveData data: NSData!, withTag tag: Int, fromHost host: String!, port: UInt16) -> Bool {
        let info = NSString(data: data, encoding: NSUTF8StringEncoding)! as String
        print(info)
        if info == "Find a match"{
            sendmsg()
        }
        
        return false
    }
    
    func connect(){
        do{
            try socket.connectToHost(IP, onPort: UInt16(SERVER_TCP_PORT))
            socket.writeData("\(UDP_PORT)".dataUsingEncoding(NSUTF8StringEncoding), withTimeout: 2000, tag: 1)
        }catch{
            print(error)
        }
        
    }
    
    func onSocket(sock: AsyncSocket!, didWriteDataWithTag tag: Int) {
        if tag == 1{
            socket.disconnect()
        }
    }

    
    override var representedObject: AnyObject? {
        didSet {
        // Update the view, if already loaded.
        }
    }


}

