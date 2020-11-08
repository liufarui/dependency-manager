# dependency-manager
A dependency manager service ,can export maven dependency to off-the-shelf graph database (Neo4J). 

#### Why do we need such a system

I don’t know if you have any thoughts. In a company that may have thousands of modules/products, how to solve the following problems when there are more interdependencies between modules:
1. What is the impact after we remove a component that has ended its life cycle?
2. After the code is modified, which dependency test cases should we run?
3. In a deployed system, which version of the module will we ultimately use?
4. Does anyone use a high-risk version of the library?

The core principle is that in the entire development cycle of all products, while modifying a certain dependency, it will not cause any impact on other products.

#### How to save dependencies

Previously, I considered using key-value database and non-relational database to store related dependencies, but the effect was not satisfactory. Finally, I chose the graph database (Neo4J).
This has three advantages:

1. For any module, whether it depends on or is dependent on other modules, it can be intuitively reflected in the database;
2. For any module, its dependent lower-level modules and upper-level modules can be quickly exported;
3. There can be a better reflection of cross-level dependencies.

#### How to use

First execute `**mvn clean install/package**` in the project directory;
Then enter the target directory and execute:

`java -jar dependency-manager-0.0.1-SNAPSHOT-jar-with-dependencies.jar *D:/workspace/so bolt://localhost:7687 neo4j neo4j Module org.spring`

The first parameter specifies `*the pom file path*` (default recursive to 20 levels, beyond which can not be recognized);
The second parameter specifies `*the url of the database*`, the third parameter specifies `*the database user name*`, and the fourth parameter specifies `*the database password*`;
The fifth parameter specifies whether the dependency relationship needs to be exported is `*projects or modules*`. If module is specified, all modules that meet the conditions will be exported. If only project is specified, only the dependency relationship between all products will be exported.
The sixth parameter specifies the conditions that the module needs to meet. You can specify the `prefix, infix, and suffix`, such as spring*, which refers to all packages starting with spring. This way you can remove some packages that we don’t care about, such as org-related, spring Frame-related can greatly reduce our workload.

Of course, you can also directly execute:
	`java -jar dependency-manager-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
Command to get help.

#### Improve

There will be several improvements as follows:
1. Dependency Manager UI visualization, you can directly display the results to the user through the page;
2. Update dependencies in an automated way;
3. Automatically issue warnings of circular dependencies and obsolete library usage according to the usage of the module;
4. It can be used across platforms, not limited to Java projects managed by Maven.

#### Chinese document

At the same time, I published the introduction in Chinese on the cnblogs in China. 

URL:  https://www.cnblogs.com/liuxia912/p/13945023.html

If you are interested, welcome to communicate with me.

And if you have any questions, you can contact me by email.

Email: liufr.dut@gmail.com

