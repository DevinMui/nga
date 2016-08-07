//
//  settings.swift
//  bluetooth
//
//  Created by Jesse Liang on 8/7/16.
//  Copyright © 2016 Jesse Liang. All rights reserved.
//



import UIKit


class settings: UIViewController {
    
    @IBOutlet weak var label: UILabel!
    
    let email = NSUserDefaults.standardUserDefaults().stringForKey("email")! as String
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        label.text = "Email: \(email)"
        
    }
    
    @IBAction func signOut(sender: UIButton) {
        
        NSUserDefaults.standardUserDefaults().setBool(false, forKey: "loggedin")
        
        performSegueWithIdentifier("logout", sender: nil)
    }
    
}