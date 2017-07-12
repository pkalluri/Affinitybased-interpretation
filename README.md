# Affinity-based Interpretation of Social Scenarios

We present a computational agent that is able to read a social scenario and deploy Bayesian inference to deduce the affinities (Friend, Neutral, or Enemy) of the scenario’s constituent relationships.

Subsequently, the agent is able to read possible interpretations of the scenario and choose which interpretation it believes to be correct.

The agent is typically tested by administering [TriangleCOPA challenge problems](https://github.com/asgordon/TriangleCOPA) consisting of a scenario, a correct interpretation, and an incorrect interpretation.

## Quick Demos

### To administer a scenario

Run ```Simulation``` with arguments ```s y \Scenario.txt \Knowledge.txt \Nonagents.txt C BT```

This spawns a verbose agent, prompts the agent to read the scenario in ```\Scenario.txt```(applying the knowledge in ```\Knowledge.txt``` and ```\Nonagents.txt```), and specifically queries for the agent’s belief about the relationship between ```C``` and ```BT```.

As the agent reads the scenario, it logs each event, its fixed knowledge about the action in the event, and its current beliefs about the scenario’s constituent relationships.
```
                 (Friend|Neutral|Enemy)   (Friend|Neutral|Enemy) 
Event            Action R.O.D.            Beliefs about relationships 
----------------------------------------------------------------
BT argueWith C   25%|25%|50%              (BT,C):17%|17%|67% 
C turn           20%|40%|40%              (BT,C):02%|20%|78% 
C exit           33%|33%|33%              (BT,C):02%|20%|78% 
Reflecting                                (BT,C):02%|20%|78% 
```
When the agent finishes reading the scenario, the last log entry indicates that the agent believes with 78% confidence that the relationship between BT and C is an Enemy relationship.

### To administer TriangleCOPA challenge problems

Run ```Simulation``` with arguments ```t y \Tricopa-Tasks.txt \Knowledge.txt \Nonagents.txt \Tricopa-Answers.txt```

This spawns a verbose agent, prompts the agent to read and answer the TriangleCOPA tasks in ```\Tricopa-Tasks.txt``` (again applying the knowledge in ```\Knowledge.txt``` and ```\Nonagents.txt```), and evaluates the agent’s answers against the gold-standard answers in ```\Tricopa-Answers.txt```.

For each TriangleCOPA task, the agent first reads the scenario and logs its evolving beliefs about the scenario’s constituent relationships (as before).
```
****************************************************************

TASK 5
                 (Friend|Neutral|Enemy)   (Friend|Neutral|Enemy) 
Event            Action R.O.D.            Beliefs about relationships 
----------------------------------------------------------------
BT inside        33%|33%|33%               
C inside         33%|33%|33%              (BT,C):33%|33%|33% 
BT argueWith C   25%|25%|50%              (BT,C):10%|10%|80% 
BT exit          33%|33%|33%              (BT,C):10%|10%|80% 
BT close DOOR    25%|25%|50%              (BT,C):00%|00%|99% 
C moveTo CORNER  33%|33%|33%              (BT,C):00%|00%|99% 
Reflecting                                (BT,C):00%|00%|99% 
```
Next, the agent reads the two possible interpretations of the scenario and logs each event, its fixed knowledge about the action in the event, and the conditional probability p of the event given the agent’s beliefs about the TriangleCOPA scenario. This enables the agent to calculate the conditional probability P of each interpretation.
```
Possible event   Action R.O.D.            p 
----------------------------------------------------------------
C happy          50%|25%|25%              25% 
                                          P=25% 

Possible event   Action R.O.D.            p 
----------------------------------------------------------------
C unhappy        25%|25%|50%              50% 
                                          P=50%  
```
When the the agent finishes reading the possible interpretations, it chooses the interpretation with the higher conditional probability:
```
The agent says: "I choose interpretation 2."
```
Finally, the agent’s answer is evaluated against the gold-standard answer:
```
CORRECT

****************************************************************
```

## General Instructions

### To administer a scenario
Run ```Simulation``` with arguments 

```s verbose scenario-file-path knowledge-file-path nonagents—file-path```

```s``` indicates **stand-alone scenario mode**. Set the remaining arguments as follows:

```verbose``` - ```y``` (or ```n```) to indicate that the agent should (or should not) be verbose

```scenario-file-path``` - the relative path of a **Scenario File** containing a scenario in logical literal form

```knowledge-file-path``` - the relative path of a **Knowledge File** containing knowledge about actions

```nonagents-file-path``` - the relative path of an **Nonagents File** containing known non-agents

### To administer a challenge problem
Run ```Simulation``` with arguments 

```t [verbose] [tricopa-tasks-file-path] [knowledge-file-path] [nonagents-file-path] [tricopa-answers-file-path]```

```t``` indicates **TriangleCOPA-style challenge problems mode**. Set the remaining arguments as follows:

```verbose``` - ```y``` (or ```n```) to indicate that the agent should (or should not) be verbose

```ticopa-tasks-file-path``` - the relative path of a **Tricopa Tasks File** containing TriangleCOPA challenge problems in logical literal form

```knowledge-file-path``` - as above

```nonagents-file-path``` - as above

```tricopa-answers-file-path``` - the relative path of an **Tricopa Answers File** containing answers to the TriangleCOPA challenge problems

### Files

A **Scenario File** contains a scenario in logical literal form.
Each line describes an event and must take the form ```(action’ e# [actor] [acted-upon])```. For example: ```(argueWith' E12 Alice Bob)``` denotes the event *Alice argues with Bob* and ```(exit' E34 Alex)``` denotes the event *Alex exits*.

A **Knowledge File** contains knowledge about actions. Each line describes in what type(s) of relationship(s) a specified action is likely to occur and must take the form ```action [F][N][E]```. For example: ```relaxed FN``` denotes the knowledge that *relaxed is likely to occur in Friend or Neutral relationships*. 

A **Nonagents File** contains known non-agents. Each line names one known non-agent. For example: ```DOOR``` denotes the knowledge that *DOOR is a known non-agent*. 

A **Tricopa Tasks File** contains TriangleCOPA challenge problems in logical literal form. For an example, see ```Tricopa-Tasks.txt```. 

**Tricopa Answers File** contains answers to TriangleCOPA challenge problems. For an example, see ```Tricopa-Answers.txt```. 


## To Learn More about this System

Please see the paper documenting this system:

 *P. Kalluri, P. Gervas. Relationship Affinity-based Interpretation of Triangle Social Scenarios. International Conference on Agents and Artificial Intelligence, 2017.*

## Author

Pratyusha Kalluri
