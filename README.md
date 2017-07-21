# Affinity-based Interpretation of Social Scenarios

We present a computational agent that is able to read a social scenario (typically in logical literal form or optionally in natural language) and deploy Bayesian inference to deduce the affinities (Friend, Neutral, or Enemy) of the scenario’s constituent relationships.

Subsequently, the agent is able to read possible interpretations of the scenario and choose which interpretation it believes to be correct.

The agent is typically tested by administering [TriangleCOPA challenge problems](https://github.com/asgordon/TriangleCOPA) consisting of a scenario, a correct interpretation, and an incorrect interpretation in logical literal form.

## Setup

Because this repository includes large external jars (the Stanford NLP parser and models), setup must be done as follows:
1. Install [git lfs](https://git-lfs.github.com/)
2. To clone the project from the command line, change the working directory to the directory where you would like the project folder to appear, then run 
```
git lfs clone  https://github.com/pkalluri/Affinitybased-interpretation
```
3. To compile the project from the command line, change the working directory to the project folder, then run
```
javac -cp src/:lib/* -d bin/ src/*
```

## Quick Demos

### To administer a short logical literal scenario

Run
```
java -cp bin/:. Simulation s -v files/Scenario.txt files/Knowledge.txt files/Scenario-Characters.txt Ria Jay
```

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
### To administer logical literal Macbeth and natural language Macbeth

To administer logical literal Macbeth, run
```
java -cp bin/:. Simulation s -v files/Macbeth-Logic.txt files/Macbeth-Logic-Knowledge.txt files/Macbeth-Logic-Characters.txt LM M
```

You will see:

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

To administer natural language Macbeth, run
```
java -cp bin/:lib/*:. Simulation s -v -nl files/Macbeth-NL.txt files/Macbeth-NL-Knowledge.txt files/Macbeth-NL-Characters.txt LadyMacbeth Macbeth
```
First, you will see the system parse natural language sentences, extracting events, for example:
```
Sentence: LadyMacbeth is Macbeth's wife.
Extracted events: [LadyMacbeth isWifeOf Macbeth]
```
Then, you will see:
```
                                 (Friend|Neutral|Enemy)   (Friend|Neutral|Enemy) 
Event                            Action R.O.D.            Beliefs about relationships 
----------------------------------------------------------------
LadyMacbeth isWifeOf Macbeth     50%|25%|25%              Macbeth&LadyMacbeth:67%|17%|17% 
Macbeth isSuccessorOf Duncan     33%|33%|33%              Macbeth&LadyMacbeth:67%|17%|17% 
Macbeth defeats Cawdor           25%|25%|50%              Macbeth&LadyMacbeth:67%|17%|17% 
Duncan becomesHappy              50%|25%|25%              Macbeth&LadyMacbeth:67%|17%|17% 
Macbeth talksWith witches        33%|33%|33%              Macbeth&LadyMacbeth:67%|17%|17% 
witches predict                  33%|33%|33%              Macbeth&LadyMacbeth:67%|17%|17% 
witches predict                  33%|33%|33%              Macbeth&LadyMacbeth:67%|17%|17% 
witches predict                  33%|33%|33%              Macbeth&LadyMacbeth:67%|17%|17% 
witches astonish Macbeth         33%|33%|33%              Macbeth&LadyMacbeth:67%|17%|17% 
Duncan rewards Macbeth           50%|25%|25%              Macbeth&LadyMacbeth:67%|17%|17% 
LadyMacbeth isGreedy             25%|25%|50%              Macbeth&LadyMacbeth:67%|17%|17% 
LadyMacbeth wants                33%|33%|33%              Macbeth&LadyMacbeth:67%|17%|17% 
LadyMacbeth persuades            50%|25%|25%              Macbeth&LadyMacbeth:67%|17%|17% 
Macbeth loves LadyMacbeth        50%|25%|25%              Macbeth&LadyMacbeth:100%|00%|00% 
Macbeth wantsPlease LadyMacbeth  50%|25%|25%              Macbeth&LadyMacbeth:100%|00%|00% 
LadyMacbeth plans                33%|33%|33%              Macbeth&LadyMacbeth:100%|00%|00% 
Macbeth plans                    33%|33%|33%              Macbeth&LadyMacbeth:100%|00%|00% 
Macbeth invites Duncan           50%|25%|25%              Macbeth&LadyMacbeth:100%|00%|00% 
Duncan praises Macbeth           50%|25%|25%              Macbeth&LadyMacbeth:100%|00%|00% 
Duncan goesTo bed                33%|33%|33%              Macbeth&LadyMacbeth:100%|00%|00% 
Macbeth murders guards           25%|25%|50%              Macbeth&LadyMacbeth:100%|00%|00% 
Macbeth stabs Duncan             25%|25%|50%              Macbeth&LadyMacbeth:100%|00%|00% 
Macduff fleesTo England          25%|25%|50%              Macbeth&LadyMacbeth:100%|00%|00% 
Macbeth kills LadyMacduff        25%|25%|50%              Macbeth&LadyMacbeth:100%|00%|00% 
Macbeth hallucinates             33%|33%|33%              Macbeth&LadyMacbeth:100%|00%|00% 
LadyMacbeth becomesDistraught    25%|25%|50%              Macbeth&LadyMacbeth:100%|00%|00% 
LadyMacbeth hallucinates         33%|33%|33%              Macbeth&LadyMacbeth:100%|00%|00% 
LadyMacbeth kills herself        25%|25%|50%              Macbeth&LadyMacbeth:00%|00%|100% 
Macduff attacks Macbeth          25%|25%|50%              Macbeth&LadyMacbeth:00%|00%|100% 
Macbeth refusesSurrender         25%|25%|50%              Macbeth&LadyMacbeth:00%|00%|100% 
Macduff kills Macbeth            25%|25%|50%              Macbeth&LadyMacbeth:00%|00%|100% 
Reflecting                                                Macbeth&LadyMacbeth:00%|00%|100% 
```

In both cases, notice how the agent's perception of the relationship between Lady Macbeth (```LM```/```LadyMacbeth```) and Macbeth (```M```/```Macbeth```) shifts over time.

You can swap out Lady Macbeth (```LM```/```LadyMacbeth```) and Macbeth (```M```/```Macbeth```) for other characters, such as Lady Macduff (```LMD```/```LadyMacduff```), Macduff (```MD```/```Macduff```), King Duncan (```KD```/```Duncan```), etc.
By omitting characters from your command, you can see the story read without focussing the agent’s attention on any specific relationship.

You might notice that some relationships in Macbeth seem to be less accurately modeled than relationships modeled in the above short scenario and the below TriangleCOPA challenge problems. We hypothesize that this is because Macbeth is a longer, evolving story not a short, simple, scenario, so the simulated agent may accumulate a large quantity of evidence for a particular underlying affinity (e.g. Macbeth and Duncan are happy, invite, and praise certainly leads to the near-certain belief that they are friends),  and an increasingly large quantity of evidence is necessary to overturn that belief (e.g. ```Macbeth stabs Duncan``` is a single event and does not overturn the near-certain belief that Macbeth and Duncan are friends).

### To administer TriangleCOPA challenge problems

Run
```
java -cp bin/:. Simulation t -v files/Tricopa-Tasks.txt files/Knowledge.txt files/Tricopa-Characters.txt files/Tricopa-Answers.txt files/Tricopa-Exclude.txt
```

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
ON THE 80 TASKS, THE AGENT ANSWERED 65/80=81%
ON THE 65 TASKS ANSWERED, THE AGENT CORRECTLY ANSWERED 53/65=82%
```

## General Use

### To administer a scenario
Run
```
java -cp bin/:lib/*:. Simulation s [-v] [-nl] scenario knowledge characters [c1 c2]
```

```s``` indicates **stand-alone scenario mode**. Set the remaining arguments as follows:

```-v``` - (optional) indicates the agent should be verbose

```-nl``` - (optional) indicates that the scenario is in natural language. If omitted, the scenario is assumed to be in logical literal form.

```scenario``` - the relative path of a **Scenario File** containing a scenario

```knowledge``` - the relative path of a **Knowledge File** containing knowledge about actions

```characters ``` - the relative path of a **Characters File** listing all characters

```c1 c2``` - (optional) the names of two characters in the scenario. The agent will focus on the relationship between these two characters: if verbose the agent will limit itself to logging only events relevant to at least one of these characters and logging only beliefs about this relationship; when finished reading the scenario, the agent will state its final beliefs about this relationship.

### To administer challenge problems
Run
```
java -cp bin/:. Simulation t [-v] tricopatasks knowledge characters tricopaanswers [tricopaexcude]
```

```t``` indicates **TriangleCOPA-style challenge problems mode**. Set the remaining arguments as follows:

```-v``` - see above

```tricopatasks``` - the relative path of a **Tricopa Tasks File** containing TriangleCOPA challenge problems in logical literal form

```knowledge``` - see above

```characters``` - see above

```tricopaanswers``` - the relative path of a **Tricopa Answers File** containing answers to the TriangleCOPA challenge problems

```tricopaexcude``` - (optional) the relative path of a **Tricopa Exclude File** containing task numbers to exclude

### To programmatically setup required files in preparation for administering a new scenario
Run
```
java -cp bin/:lib/*:. Simulation s -setup [-nl] scenario knowledge characters
```

(Note that setting up required files can also be done manually, and this command is merely for user convenience)

```s``` see above. Set the remaining arguments as follows:

```-setup``` - indicates **setup mode**

```-nl``` - see above

```scenario``` - see above

```knowledge``` - the relative path pointing to where the system should generate a new template **Knowledge File**. In this generated file, all actions will be listed with uninformative knowledge. You, the user, should then manually update the template by adding ```F```, ```N```, and ```E``` characters, making the file an accurate Knowledge File.

```characters ``` - the relative path pointing to where the system should generate a new template **Characters File**. In this generated file, all possible characters will be listed. You, the user, should then manually update the template by removing non-characters, making the file an accurate Characters File.

### Files

A **Scenario File** contains a scenario. If the Scenario File is in logical literal form, each line describes an event and must take the form ```(action’ e# actor [acted-upon])```. For example: ```(playWith’ E12 Luca Lyra)``` denotes the event ”Luca plays with Lyra” and ```(dance’ E34 Alex)``` denotes the event ”Alex dances”. For an example file in logical literal form, see ```Scenario.txt```. If the Scenario File is in natural language, each line contains one natural language sentence. For an example file in natural language, see ```Macbeth-NL.txt```.

A **Knowledge File** contains knowledge about actions. Each line describes in what type(s) of relationship(s) a specified action is likely to occur and must take the form ```action [F][N][E]```. For example: ```relaxed FN``` denotes the knowledge that ```relaxed``` is likely to occur in Friend or Neutral relationships. For an example file, see ```Knowledge.txt```.

A **Characters File** lists all characters. Each line names one known character. For example: ```Sophia``` denotes the knowledge that ```Sophia``` is a character. For chacters with spaces in their name, omit the spaces. For example: ```LadyMacbeth``` can denote the knowledge that ```Lady Macbeth``` is a character. For an example file, see ```Scenario-Characters.txt``` or ```Macbeth-NL-Characters.txt```.

A **Tricopa Tasks File** contains TriangleCOPA challenge problems in their logical literal form. The file format is that used by previous TriangleCOPA work. For an example file, see ```Tricopa-Tasks.txt```. 

A **Tricopa Answers File** contains answers to TriangleCOPA challenge problems. Each line indicates one answer and takes the form ```task-number letter-of-correct-choice```. For example: ```1 a``` indicates the answer to TriangleCOPA task ```1``` is choice ```a``` (the first choice). For an example file, see ```Tricopa-Answers.txt```. 

A **Tricopa Exclude File** contains task numbers to exclude. Each line contains any number of task numbers separated by space. For example: ```12 34``` indicates that TriangleCOPA tasks ```12``` and ```34```  should be excluded, i.e. should not be administered to the agent. Comment lines are lines beginning with ```//``` and are ignored. For an example file, see ```Tricopa-Exclude.txt```. 

## For More Information

### On the theory

To learn more, please see the paper documenting this system:

[*P. Kalluri, P. Gervas. Relationship Affinity-based Interpretation of Triangle Social Scenarios. International Conference on Agents and Artificial Intelligence, 2017.*](http://www.scitepress.org/DigitalLibrary/PublicationsDetail.aspx?ID=l5g7xHmN%2fRM%3d&t=1)
 
### On the code

For anyone interested in digging into the code, the high-level code structure is as follows:

The ```Simulation``` class spawns an ```AffinitybasedAgent``` and administers to the ```AffinitybasedAgent``` a ```Scenario``` or ```TricopaTasks``` (both containing ```ActionEvents```). To interpret these ```ActionEvents```, the ```AffinitybasedAgent``` applies its fixed knowledge of ```ActionRODs``` (relative observation distributions) and  begins building an ```AffinitybasedWorldModel```. An ```AffinitybasedWorldModel``` models ```Pairs``` of encountered agents as ```SymmetricRelationshipModels```; under the hood, an ```AffinitybasedWorldModel``` is essentially mapping ```Pairs``` of encountered agents to an evolving, probabilistic understanding of the ```Pair```’s ```RelationshipType``` (Friend, Neutral, or Enemy). Once the ```AffinitybasedAgent``` has completed interpretation, it is able to query its built ```AffinitybasedWorldModel``` and state its beliefs about the ```Scenario``` or complete the ```TricopaTask```.

### On the NLP

This system originally operated on only logical literal scenarios. Optionally operating on natural language scenarios is a later addition to the system and remains in a prototype phase. The system parses natural language sentences into typed dependencies using the **Stanford NLP parser**, then extracts events from the typed dependencies using a system-defined procedure. See the ```NLPUtility``` class.

## Author

Pratyusha Kalluri

## Acknowledgements

Enormously grateful that I was able to do this work at Universidad Complutense de Madrid under the guidance of Pablo Gervas! Also very grateful for the financial support of the MIT-Spain Program of the MIT International Science and Technology Initiatives (MISTI) and the IDiLyCo project funded by the Spanish Ministry of Economy, Industry, and Competitiveness.
