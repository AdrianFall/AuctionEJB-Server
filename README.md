Deployment Instructions
======================
The project used Glassfish 3.1 Java EE application server for deployment, with configured connection pooling to derby bundled Glassfish (i.e. starting derby from glassfish directory, using cmd commands: asadmin, start-database) database and JPA 2.0 or 2.1 with EclipseLink 2.5.x platform.


Program Description
===================

The program is a prototype of an auction website utilizing the following technologies: Enterprise JavaBeans, Servlets, JavaServer Pages, Persistence Storage (Derby) and JavaScript. 

The functionality of the website comprises of the following:  

•	Registration – along with email sending upon successful registration.

•	Login

•	Inserting new items

•	List of user items

•	Inserting new auction – along with sending email to the auction creator.

•	List of user auctions’ – the list includes the status of the auction (i.e. finished or ongoing) along with information whether somebody has won/is winning the auction and the winning price (if won).

•	Global list of auctions – list of unexpired auctions with the optionality to browse by category along with information about the starting price or current bid price, time left of the auction and hyperlink to the bidding.

•	Bidding of an auction – with a countdown time of the auction, information about the current/starting price and the functionality to bid on the auction.

•	List of user bids’ – the list of the users’ bids categorized into won and ongoing. The ongoing category comprises the list of active auctions that the user has bid at, along with the information whether the bid is still winning or somebody else is winning that auction.

•	Auction expiration – upon an auction being created there is a thread started that will handle the expiration of an auction on time and send email messages to the owner of the auction, as well as to the winner of the auction (if one exists). Since the mentioned method of starting a thread to handle expiration will grant only the optimal effect if the glassfish server remains alive (i.e. doesn’t stop or crash), there was an additional “lazy expiration” method put in place which will perform a check on the auction whether it’s expired every time somebody looks into his/her auctions, global auctions and attempts to bid on an auction. Therefore there is no possibility that any user will bid on an expired auction and all auctions will eventually expire.
