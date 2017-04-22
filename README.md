
** Issue Detail Page 2 **

Get UserType: SAMPLING|MERCHANDISING
MERCHANDISING:
    if ackAt == null : problem is not acknowledged yet
        if(user.buyers.contains(issue.dField2)) : If user is concerned to this issue
            if processingAt > 1 : Both level1 and level2 user can acknowledge
                if user.level == LEVEL1 || user.level == LEVEL2
                    add ackButton
            else : Only level 1 user can acknowledge
                if user.level == LEVEL1
                    add ackButton
    else if fixAt == null: problem is acknowledged but not fixed yet
        if(user.buyers.contains(issue.dField2)) : If user is concerned to this issue
            if processingAt > 1 : level2 should fix the issue
                if user.level == LEVEL2
                    add fixButton
            else : level 1 should fix the issue
                if user.level == LEVEL1
                    add fixButton


**Notification 2 Page**

Get UserType: SAMPLING|MERCHANDISING
SAMPLING:
    Get all Issue raised by this user
    if fixAt == null : Issue not fixed yet
        if ackAt != null : Issue is acknowledged
            User X acknowleged Problem X of TeamY:BuyerZ.
        else    : Issue is raised
            Problem X for dField1 Y:Buyer Z was raised by you.
MERCHANDISING:
    Get All issues for which user is related to.
        if fixAt != null : problem is fixed
            ProblemX of teamY:BuyerZ is resolved.
        else if ackAt != null : problem is acknowledged
            ProblemX of teamY:BuyerZ is acknowledged by {ackBy == urId? you : ackByUser}.
        else
            ProblemX of teamY:BuyerZ is raised by {raisedByUser}.
