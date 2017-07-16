# Affinity-based Interpretation of Social Scenarios

We present a computational agent that is able to read a social scenario and deploy Bayesian inference to deduce the affinities (Friend, Neutral, or Enemy) of the scenario’s constituent relationships.

Subsequently, the agent is able to read possible interpretations of the scenario and choose which interpretation it believes to be correct.

The agent is typically tested by administering [TriangleCOPA challenge problems](https://github.com/asgordon/TriangleCOPA) consisting of a scenario, a correct interpretation, and an incorrect interpretation.

To learn more, please see the paper documenting this system:

 *P. Kalluri, P. Gervas. Relationship Affinity-based Interpretation of Triangle Social Scenarios. International Conference on Agents and Artificial Intelligence, 2017.*

## Setup

Compile the project.
To do this from the command line, change the working directory to the project folder then run

```javac -cp src/:lib/* -d bin/ src/*```

## Quick Demos

### To administer a short scenario

Run ```java -cp bin/:. Simulation s -v files/Scenario.txt files/Knowledge.txt files/Scenario-Characters.txt Ria Jay```

This spawns a verbose agent and prompts the agent to read the scenario in ```Scenario.txt```(applying the knowledge in ```Knowledge.txt``` and ```Scenario-Characters.txt```), focusing its attention on the relationship between ```Ria``` and ```Jay```.

As the agent reads the scenario, it logs each event relevant to either ```Ria``` or ```Jay```, its fixed knowledge about the action in the event, and its current beliefs about the relationship between ```Ria``` and ```Jay```.
```
                         (Friend|Neutral|Enemy)   (Friend|Neutral|Enemy) 
Event                    Action R.O.D.            Beliefs about relationships 
----------------------------------------------------------------
Jay argueWith Ria        25%|25%|50%              Jay&Ria:17%|17%|67% 
Ria turn                 33%|33%|33%              Jay&Ria:17%|17%|67% 
Ria exit                 33%|33%|33%              Jay&Ria:17%|17%|67% 
Reflecting                                        Jay&Ria:17%|17%|67% 
```
When the agent finishes reading the scenario, the last log entry indicates the agent’s final beliefs.

Finally, the agent states its beliefs about the relationship between ```Ria``` and ```Jay```:

```
I believe that the relationship between Jay&Ria is a enemy relationship with 67% confidence.
```
### To administer Macbeth

Run ```java -cp bin/:. Simulation s -v files/Macbeth-Logic.txt files/Macbeth-Knowledge.txt files/Nonagents.txt LM M```

Notice how the agent's perception of the relationship between ```LM``` (Lady Macbeth) and ```M``` (Macbeth) shifts over time:
```
                         (Friend|Neutral|Enemy)   (Friend|Neutral|Enemy) 
Event                    Action R.O.D.            Beliefs about relationships 
----------------------------------------------------------------
LM isWife M              50%|25%|25%              LM&M:67%|17%|17% 
M isSuccessor KD         33%|33%|33%              LM&M:67%|17%|17% 
M defeats C              25%|25%|50%              LM&M:67%|17%|17% 
KD becomesHappy          50%|25%|25%              LM&M:67%|17%|17% 
M talks W                33%|33%|33%              LM&M:67%|17%|17% 
W predict                33%|33%|33%              LM&M:67%|17%|17% 
W predict                33%|33%|33%              LM&M:67%|17%|17% 
W predict M              33%|33%|33%              LM&M:67%|17%|17% 
W predict M              33%|33%|33%              LM&M:67%|17%|17% 
M rewards                50%|25%|25%              LM&M:67%|17%|17% 
LM asksForMorePower M    50%|25%|25%              LM&M:100%|00%|00% 
LM persuades M           50%|25%|25%              LM&M:100%|00%|00% 
M loves LM               50%|25%|25%              LM&M:100%|00%|00% 
M wantsToPlease LM       50%|25%|25%              LM&M:100%|00%|00% 
LM plotsToMurder KD      25%|25%|50%              LM&M:100%|00%|00% 
M plotsToMurder KD       25%|25%|50%              LM&M:100%|00%|00% 
M invitesToDinner KD     50%|25%|25%              LM&M:100%|00%|00% 
KD compliments M         50%|25%|25%              LM&M:100%|00%|00% 
KD goesToBed             33%|33%|33%              LM&M:100%|00%|00% 
M murders G              25%|25%|50%              LM&M:100%|00%|00% 
M murders KD             25%|25%|50%              LM&M:100%|00%|00% 
MD flees                 25%|25%|50%              LM&M:100%|00%|00% 
M murders LMD            25%|25%|50%              LM&M:100%|00%|00% 
M hallucinates           33%|33%|33%              LM&M:100%|00%|00% 
LM becomesDistraught     25%|25%|50%              LM&M:100%|00%|00% 
LM hasBadDreams          25%|25%|50%              LM&M:100%|00%|00% 
LM hallucinates          33%|33%|33%              LM&M:100%|00%|00% 
LM commitesSuicide       25%|25%|50%              LM&M:00%|00%|100% 
MD attacks               25%|25%|50%              LM&M:00%|00%|100% 
MD curses M              25%|25%|50%              LM&M:00%|00%|100% 
M refusesToSurrender     25%|25%|50%              LM&M:00%|00%|100% 
M kills                  25%|25%|50%              LM&M:00%|00%|100% 
Reflecting                                        LM&M:00%|00%|100% 
```
You can swap out ```LM``` or ```M``` for other characters, such as ```LMD``` (Lady Macduff), ```MD``` (Macduff), etc.

Or you can run ```java -cp bin/:. Simulation s -v files/Macbeth-Logic.txt files/Macbeth-Knowledge.txt files/Nonagents.txt``` (omitting characters) to see the story read without focussing the agent’s attention on any specific relationship.

### To administer TriangleCOPA challenge problems

Run ```java -cp bin/:. Simulation t -v files/Tricopa-Tasks.txt files/Knowledge.txt files/Tricopa-Characters.txt files/Tricopa-Answers.txt files/Tricopa-Exclude.txt```

This spawns a verbose agent, prompts the agent to read and answer the TriangleCOPA tasks in ```Tricopa-Tasks.txt``` (except the tasks listed in ```Tricopa-Exclude.txt```) (applying the knowledge in ```Knowledge.txt``` and ```Tricopa-Characters.txt```), and evaluates the agent’s answers against the gold-standard answers in ```Tricopa-Answers.txt```.

For each TriangleCOPA task, the agent first reads the TriangleCOPA scenario and logs its evolving beliefs (as before).
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
And the agent’s answer is evaluated against the gold-standard answer:
```
CORRECT

****************************************************************
```
After the agent has finished the last task, a score statement is printed:
```
ON THE 80 TASKS, THE AGENT ANSWERED 66/80=82%
ON THE 66 TASKS ANSWERED, THE AGENT CORRECTLY ANSWERED 53/66=80%
```

## General Instructions

### To administer a scenario
Run ```java -cp bin/:. Simulation s [-v] scenario knowledge characters [c1 c2]```

```s``` indicates **stand-alone scenario mode**. Set the remaining arguments as follows:

```-v``` - (optional) indicates the agent should be verbose

```scenario``` - the relative path of a **Scenario File** containing a scenario in logical literal form

```knowledge``` - the relative path of a **Knowledge File** containing knowledge about actions

```characters ``` - the relative path of a **Characters File** listing all characters

```c1 c2``` - (optional) the names of two characters in the scenario. The agent will focus on the relationship between these two characters: if verbose the agent will limit itself to logging only events relevant to at least one of these characters and logging only beliefs about this relationship; when finished reading the scenario, the agent will state its final beliefs about this relationship.

### To administer challenge problems
Run ```java -cp bin/:. Simulation t [-v] tricopatasks knowledge characters tricopaanswers [tricopaexcude]```

```t``` indicates **TriangleCOPA-style challenge problems mode**. Set the remaining arguments as follows:

```-v``` - see above

```tricopatasks``` - the relative path of a **Tricopa Tasks File** containing TriangleCOPA challenge problems in logical literal form

```knowledge``` - see above

```characters``` - see above

```tricopaanswers``` - the relative path of a **Tricopa Answers File** containing answers to the TriangleCOPA challenge problems

```tricopaexcude``` - (optional) the relative path of a ***Tricopa Exclude File*** containing task numbers to exclude

### Files

A **Scenario File** contains a scenario in logical literal form.
Each line describes an event and must take the form ```(action’ e# actor [acted-upon])```. For example: ```(playWith’ E12 Luca Lyra)``` denotes the event ”Luca plays with Lyra” and ```(dance’ E34 Alex)``` denotes the event ”Alex dances”. For an example file, see ```Scenario.txt```.

A **Knowledge File** contains knowledge about actions. Each line describes in what type(s) of relationship(s) a specified action is likely to occur and must take the form ```action [F][N][E]```. For example: ```relaxed FN``` denotes the knowledge that ```relaxed``` is likely to occur in Friend or Neutral relationships. For an example file, see ```Knowledge.txt```.

A **Characters File** lists all characters. Each line names one known character. For example: ```Sophia``` denotes the knowledge that ```Sophia``` is a character. For chacters with spaces in their name, omit the spaces. For example: ```LadyMacbeth``` can denote the knowledge that ```Lady Macbeth``` is a character. For an example file, see ```Scenario-Characters.txt``` or ```Macbeth-Characters.txt```.

A **Tricopa Tasks File** contains TriangleCOPA challenge problems in their logical literal form. The file format is that used by previous TriangleCOPA work. For an example file, see ```Tricopa-Tasks.txt```. 

A **Tricopa Answers File** contains answers to TriangleCOPA challenge problems. Each line indicates one answer and takes the form ```task-number letter-of-correct-choice```. For example: ```1 a``` indicates the answer to TriangleCOPA task ```1``` is choice ```a``` (the first choice). For an example file, see ```Tricopa-Answers.txt```. 

A **Tricopa Exclude File** contains task numbers to exclude. Each line contains any number of task numbers separated by space. For example: ```12 34``` indicates that TriangleCOPA tasks ```12``` and ```34```  should be excluded, i.e. should not be administered to the agent. Comment lines are lines beginning with ```//``` and are ignored. For an example file, see ```Tricopa-Exclude.txt```. 

## The gist of the code

In case you are interested in digging into the code, the high-level code structure is as follows:

The ```Simulation``` class spawns an ```AffinitybasedAgent``` and administers to the ```AffinitybasedAgent``` a ```Scenario``` or ```TricopaTasks``` (both containing ```ActionEvents```). To interpret these ```ActionEvents```, the ```AffinitybasedAgent``` applies its fixed knowledge of ```ActionRODs``` (relative observation distributions) and  begins building an ```AffinitybasedWorldModel```. An ```AffinitybasedWorldModel``` models ```Pairs``` of encountered agents as ```SymmetricRelationshipModels```; under the hood, an ```AffinitybasedWorldModel``` is essentially mapping ```Pairs``` of encountered agents to an evolving, probabilistic understanding of the ```Pair```’s ```RelationshipType``` (Friend, Neutral, or Enemy). Once the ```AffinitybasedAgent``` has completed interpretation, it is able to query its built ```AffinitybasedWorldModel``` and state its beliefs about the ```Scenario``` or complete the ```TricopaTask```.

## Author

Pratyusha Kalluri

## Acknowledgements

Enormously grateful that I was able to do this work at the Universidad Complutense de Madrid under the guidance of Pablo Gervas! Also very grateful for the financial support of the MIT-Spain Program of the MIT International Science and Technology Initiatives (MISTI) and the IDiLyCo project funded by the Spanish Ministry of Economy, Industry, and Competitiveness
