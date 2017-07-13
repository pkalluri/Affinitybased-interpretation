# Affinity-based Interpretation of Social Scenarios

We present a computational agent that is able to read a social scenario and deploy Bayesian inference to deduce the affinities (Friend, Neutral, or Enemy) of the scenario’s constituent relationships.

Subsequently, the agent is able to read possible interpretations of the scenario and choose which interpretation it believes to be correct.

The agent is typically tested by administering [TriangleCOPA challenge problems](https://github.com/asgordon/TriangleCOPA) consisting of a scenario, a correct interpretation, and an incorrect interpretation.

To learn more, please see the paper documenting this system:

 *P. Kalluri, P. Gervas. Relationship Affinity-based Interpretation of Triangle Social Scenarios. International Conference on Agents and Artificial Intelligence, 2017.*

## Quick Demos

### To administer a short scenario

Run ```Simulation``` with arguments ```s y Scenario.txt Knowledge.txt Nonagents.txt Ria Jay```

This spawns a verbose agent, prompts the agent to read the scenario in ```Scenario.txt```(applying the knowledge in ```Knowledge.txt``` and ```Nonagents.txt```), and specifically queries for the agent’s belief about the relationship between ```Ria``` and ```Jay```.

As the agent reads the scenario, it logs each event, its fixed knowledge about the action in the event, and its current beliefs about the scenario’s constituent relationships.
```
                         (Friend|Neutral|Enemy)   (Friend|Neutral|Enemy) 
Event                    Action R.O.D.            Beliefs about relationships 
----------------------------------------------------------------
Jay argueWith Ria        25%|25%|50%              Jay&Ria:17%|17%|67% 
Ria turn                 20%|40%|40%              Jay&Ria:02%|20%|78% 
Ria exit                 33%|33%|33%              Jay&Ria:02%|20%|78% 
Reflecting                                        Jay&Ria:02%|20%|78% 
```
When the agent finishes reading the scenario, the last log entry indicates the agent’s final beliefs.

Finally, the agent responds to the posed query:

```
I believe that the relationship between Jay&Ria is a enemy relationship with 78% confidence.
```
### To administer Macbeth
Run ```Simulation``` with arguments ```s y Macbeth-Logic.txt Macbeth-Knowledge.txt Nonagents.txt LM M```

Notice how the agent's perception of the relationship between ```LM``` (Lady Macbeth) and ```M``` (Macbeth) shifts over time.
Or try swapping out ```LM``` or ```M``` for other characters, such as ```LMD``` (Lady Macduff), ```MD``` (Macduff), etc.

### To administer TriangleCOPA challenge problems

Run ```Simulation``` with arguments ```t y Tricopa-Tasks.txt \Knowledge.txt Nonagents.txt Tricopa-Answers.txt```

This spawns a verbose agent, prompts the agent to read and answer the TriangleCOPA tasks in ```Tricopa-Tasks.txt``` (again applying the knowledge in ```Knowledge.txt``` and ```Nonagents.txt```), and evaluates the agent’s answers against the gold-standard answers in ```Tricopa-Answers.txt```.

For each TriangleCOPA task, the agent first reads the scenario and logs its evolving beliefs (as before).
```
****************************************************************

TASK 5
                         (Friend|Neutral|Enemy)   (Friend|Neutral|Enemy) 
Event                    Action R.O.D.            Beliefs about relationships 
----------------------------------------------------------------
BT inside                33%|33%|33%               
C inside                 33%|33%|33%              BT&C:33%|33%|33% 
BT argueWith C           25%|25%|50%              BT&C:10%|10%|80% 
BT exit                  33%|33%|33%              BT&C:10%|10%|80% 
BT close D               25%|25%|50%              BT&C:00%|00%|99% 
C moveTo CORNER          33%|33%|33%              BT&C:00%|00%|99% 
Reflecting                                        BT&C:00%|00%|99% 
```
Next, the agent reads the two possible interpretations of the scenario and logs each event, its fixed knowledge about the action in the event, and the conditional probability ```p``` of the event given the agent’s beliefs about the TriangleCOPA scenario. This enables the agent to calculate the conditional probability ```P``` of each interpretation.
```
Possible event           Action R.O.D.            p 
----------------------------------------------------------------
C happy                  50%|25%|25%              25% 
                                                  P=25% 

Possible event           Action R.O.D.            p 
----------------------------------------------------------------
C unhappy                25%|25%|50%              50% 
                                                  P=50% 
```
When the the agent finishes reading the possible interpretations, it chooses the interpretation with the higher conditional probability:
```
I choose interpretation 2.
```
Finally, the agent’s answer is evaluated against the gold-standard answer:
```
CORRECT

****************************************************************
```

## General Instructions

### To administer a scenario
Run ```Simulation``` with arguments ```s verbose scenario knowledge nonagents [c1 c2]```

```s``` indicates **stand-alone scenario mode**. Set the remaining arguments as follows:

```verbose``` - ```y``` (or ```n```) to indicate the agent should (or should not) be verbose

```scenario``` - the relative path of a **Scenario File** containing a scenario in logical literal form

```knowledge``` - the relative path of a **Knowledge File** containing knowledge about actions

```nonagents``` - the relative path of an **Nonagents File** containing known non-agents

```[c1 c2]``` - optionally, the names of two characters in the scenario. The agent will focus on the relationship between these two characters: if verbose the agent will limit itself to logging its evolving beliefs about this relationship only; when finished reading the scenario, the agent will state its final beliefs about this relationship.

### To administer challenge problems
Run ```Simulation``` with arguments ```t verbose tricopatasks knowledge nonagents tricopaanswers```

```t``` indicates **TriangleCOPA-style challenge problems mode**. Set the remaining arguments as follows:

```verbose``` - see above

```tricopatasks``` - the relative path of a **Tricopa Tasks File** containing TriangleCOPA challenge problems in logical literal form

```knowledge``` - see above

```nonagents``` - see above

```tricopaanswers``` - the relative path of an **Tricopa Answers File** containing answers to the TriangleCOPA challenge problems

### Files

A **Scenario File** contains a scenario in logical literal form.
Each line describes an event and must take the form ```(action’ e# [actor] [acted-upon])```. For example: ```(playWith’ E12 Luca Lyra)``` denotes the event ”Luca plays with Lyra” and ```(dance’ E34 Alex)``` denotes the event ”Alex dances”. For an example file, see ```Scenario.txt```.

A **Knowledge File** contains knowledge about actions. Each line describes in what type(s) of relationship(s) a specified action is likely to occur and must take the form ```action [F][N][E]```. For example: ```relaxed FN``` denotes the knowledge that ```relaxed``` is likely to occur in Friend or Neutral relationships. For an example file, see ```Knowledge.txt```.

A **Nonagents File** contains known non-agents. Each line names one known non-agent. For example: ```DOOR``` denotes the knowledge that ```DOOR``` is a known non-agent. For an example file, see ```Nonagents.txt```.

A **Tricopa Tasks File** contains TriangleCOPA challenge problems in their logical literal form. For an example file, see ```Tricopa-Tasks.txt```. 

A **Tricopa Answers File** contains answers to TriangleCOPA challenge problems. For an example file, see ```Tricopa-Answers.txt```. 

## The gist of the code

In case you are interested in digging into the code, the high-level code structure is as follows:

The ```Simulation``` class spawns an ```AffinitybasedAgent``` and administers to the ```AffinitybasedAgent``` a ```Scenario``` or ```TricopaTasks```. To interpret either, the ```AffinitybasedAgent``` begins building an ```AffinitybasedWorldModel```. An ```AffinitybasedWorldModel``` models ```Pairs``` of encountered agents as ```SymmetricRelationshipModels```; under the hood, an ```AffinitybasedWorldModel``` is essentially mapping ```Pairs``` of encountered agents to an evolving, probabilistic understanding of the ```Pair’s``` ```RelationshipType``` (Friend, Neutral, or Enemy). During interpretation, the ```AffinitybasedAgent``` continues to update its ```AffinitybasedWorldModel```. After interpretation, the ```AffinitybasedAgent``` is able to respond to queries about the ```Scenario``` or is able to complete the ```TricopaTask```.

## Author

Pratyusha Kalluri

## Acknowledgements

Enormously grateful that I was able to do this work at the Universidad Complutense de Madrid under the guidance of Pablo Gervas! And also very grateful for the financial support of the MIT-Spain Program of the MIT International Science and Technology Initiatives (MISTI) and the IDiLyCo project funded by the Spanish Ministry of Economy, Industry, and Competitiveness

