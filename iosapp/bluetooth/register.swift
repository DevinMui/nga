//
//  login.swift
//  bluetooth
//
//  Created by Jesse Liang on 8/7/16.
//  Copyright © 2016 Jesse Liang. All rights reserved.
//

import UIKit
import Alamofire

class register: UIViewController {
    
    @IBOutlet weak var email: UITextField!
    @IBOutlet weak var cemail: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
    }
    
    @IBAction func register(sender: UIButton) {
        
        let emailtext = email.text
        let cemailtext = email.text
        
        let params = [
            "email": emailtext! as String,
            "disease": "null",
            "severity": 0,
            "doctor": "",
            "password": hash
            
        ]
        
        let headers : [String : String] = [
            "Accept": "application/json",
            "Content-Type": "application/json"
        ]
        
        if emailtext != cemailtext {
        } else {
            self.dismissViewControllerAnimated(true, completion: nil)
            
            
            
            Alamofire.request(.POST, "http://dce96ee1.ngrok.io/create_person", parameters: params as? [String : AnyObject], headers: headers, encoding: .JSON)
                .responseJSON { response in
                    if let JSON = response.result.value {
                        
                    }
            }

        }
    }
    
    @IBAction func back(sender: UIButton) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
}