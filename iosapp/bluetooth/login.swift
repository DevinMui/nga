//
//  login.swift
//  bluetooth
//
//  Created by Jesse Liang on 8/7/16.
//  Copyright © 2016 Jesse Liang. All rights reserved.
//

import UIKit


class login: UIViewController {

    @IBOutlet weak var email: UITextField!
    
    let loggedin = NSUserDefaults.standardUserDefaults().boolForKey("loggedin")
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if loggedin == true {
            performSegueWithIdentifier("login", sender: true)
        } else {
        }
        
        
    }
    
    @IBAction func login(sender: UIButton) {
        
        let emailtext = email.text! as String
        if emailtext == "" {
            
        } else {
            NSUserDefaults.standardUserDefaults().setObject(emailtext, forKey: "email")
            NSUserDefaults.standardUserDefaults().setBool(true, forKey: "loggedin")
        
            performSegueWithIdentifier("login", sender: nil)
        }
    }

}