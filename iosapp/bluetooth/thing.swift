




import Mapbox
import UIKit


class thing: UIViewController, MGLMapViewDelegate {
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let mapView = MGLMapView(frame: view.bounds)
        mapView.autoresizingMask = [.FlexibleWidth, .FlexibleHeight]
        mapView.styleURL = MGLStyle.darkStyleURLWithVersion(9)
        mapView.centerCoordinate = CLLocationCoordinate2DMake(37.3798862, -122.01248270000002)
        mapView.showsUserLocation = true
        mapView.zoomLevel = 10
        mapView.maximumZoomLevel = 17
        mapView.delegate = self
        view.addSubview(mapView)
        
        // Specify coordinates for our annotations.
        let coordinates = [
            CLLocationCoordinate2DMake(45.52245, -122.67773),
            CLLocationCoordinate2DMake(37.4, -122.014),
            CLLocationCoordinate2DMake(37.36, -122.02)
        ]
        
        // Fill an array with point annotations and add it to the map.
        var pointAnnotations = [MGLPointAnnotation]()
        for coordinate in coordinates {
            let point = MGLPointAnnotation()
            point.coordinate = coordinate
            point.title = "\(coordinate.latitude), \(coordinate.longitude)"
            pointAnnotations.append(point)
        }
        
        mapView.addAnnotations(pointAnnotations)
        
        //_ = NSTimer.scheduledTimerWithTimeInterval(0.1, target: self, selector: Selector("update"), userInfo: nil, repeats: true)
        
    }
    
    func update () {
        
        dispatch_async(dispatch_get_main_queue(), {

        })
    }
    
    // MARK: - MGLMapViewDelegate methods
    
    // This delegate method is where you tell the map to load a view for a specific annotation. To load a static MGLAnnotationImage, you would use `-mapView:imageForAnnotation:`.
    func mapView(mapView: MGLMapView, viewForAnnotation annotation: MGLAnnotation) -> MGLAnnotationView? {
        // This example is only concerned with point annotations.
        guard annotation is MGLPointAnnotation else {
            return nil
        }
        
        // Use the point annotation’s longitude value (as a string) as the reuse identifier for its view.
        let reuseIdentifier = "\(annotation.coordinate.longitude)"
        
        // For better performance, always try to reuse existing annotations.
        var annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(reuseIdentifier)
        
        // If there’s no reusable annotation view available, initialize a new one.
        if annotationView == nil {
            annotationView = CustomAnnotationView(reuseIdentifier: reuseIdentifier)
            annotationView!.frame = CGRectMake(0, 0, 40, 40)
            
            // Set the annotation view’s background color to a value determined by its longitude.
            annotationView!.backgroundColor = UIColor(hue: 0, saturation: 0.5, brightness: 1, alpha: 0.4)
        }
        
        return annotationView
    }
    
    func mapView(mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
        return true
    }
}


  