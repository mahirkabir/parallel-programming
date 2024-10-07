# Scalashop

In this project, we used parallel programming to apply a horizontal and a vertical kernel on an image to make it blur

## How to Run
- Go to the project root directory and execute `runMain scalashop.ScalaShop`
- Once the applet is opened, click on `Apply filter` button to blur
- Click on `Reload` to reset all the changes
- Change `Filter` dropdown value to shift from Horizontal Blurring to Vertical and vice versa
- Change `Radius` value to change the radius of the kernel
- Change `Tasks` value to specify how many parallel tasks will take place at once (Giving a large value might not always bring faster result for this option. Because, creating too many parallel tasks might even take more time compared to doing some of it sequentially. Change the value to find the optimum number that makes the process fastest)