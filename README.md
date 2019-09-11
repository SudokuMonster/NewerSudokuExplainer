# Sudoku Explainer 软件介绍

***Title: Brief Introduction of Software Sudoku Explainer***



## 介绍

***Title: Brief Intro***

这是一款以 Java 编写的数独分析和测试软件，官网为http://diuf.unifr.ch/pai/people/juillera/Sudoku/Sudoku.html（不过现在已经打不开了……）。

This is a Sudoku analysis and test software written by Java programming language, whose website is http://diuf.unifr.ch/pai/people/juillera/Sudoku/Sudoku.html (However the website has shut down).

![](/@Document/1.png)



以及生成题目工具：

And generator

![](/@Document/2.png)



以及技巧选择窗体：

And techinques selector

![](/@Document/3.png)

> 注意：欠一数对（Almost Locked Pair）、欠一三数组（Almost Locked Triple）、唯一矩形的扩展（Unique Rectangle Extension）目前还在开发阶段当中。
>
> Note: Technique *Almost Locked Pair*, *Almost Locked Triple* and *Unique Rectangle Extension* are under construction.



以及分析结果页面：

And... analysis information

![](/@Document/4.png)



## 程序修改和更新内容

***Title: What's Updated***

这里陈列为 Sudoku Explainer 软件添加和修改的一些功能，由于 Sudoku Explainer 软件使用了 LGPL 协议，所以所有更新修改的内容都将会公开其中的源代码。

Here displays updated and new functions in Sudoku Explainer. All project codes will be uploaded because of using LGPL protocol.


## 文件夹介绍
***Title: Intro of directories***

* `@Obsolete`：以前使用的文件，不过由于新功能的替换后，原来的文件就不再使用了，做的一个备份。

  `@Obsolete`: Older or obsolete files.
* `@Ref`：使用的一些其它 SE 变体轻应用的文件。

  `@Ref`: Files of Variant SE applets.


### 新增内容

***Title: What's New***

* 新增 W-Wing、双强链技巧（Turbot Fishes）、WXYZ-Wing、VWXYZ-Wing 和 XYZ-Wing、WXYZ-Wing 的拓展构型；
  
  Add *W-Wing*, *Two-Strong-Link Techniques (Turbot Fish, 2-String Kite & Skyscraper)*, *XYZ-Wing Extension, WXYZ-Wing, WXYZ-Wing Extension, VWXYZ-Wing*.
* 规定 W-Wing 技巧的难度系数为 4.4、XYZ-Wing 拓展构型的难度系数为 4.5、WXYZ-Wing 的难度系数为 4.6、WXYZ-Wing 拓展构型的难度系数为 4.8，VWXYZ-Wing 的难度系数为 5.0，摩天楼为 4.0、双线风筝为4.1、多宝鱼为 4.2；
  
  Define difficulty ratings of *W-Wing, XYZ-Wing Extension, WXYZ-Wing, WXYZ-Wing Extension, VWXYZ-Wing, Skyscrapers, 2-String Kite, Turbot Fish* are 4.4, 4.5, 4.6, 4.8, 5.0, 4.0, 4.1 and 4.2 respectively.
* 难度级别的显示；
  
  Add displaying difficulty level for a puzzle.
* 区分提示数和填入数值，以便后续添加可规避矩形技巧的功能；
  
  Can tell givens and modifiable values, in order to use technique *Avoidable Rectangle* which will be added in the program in the future.
* 撤销功能（该功能不是我实现的）。
  
  Add undo button (but this function is not implemented by me).

### 修改功能

***Title: What's modified***

* 出题难度系数范围的精细化；
  
  More difficulty levels when generating.
* 难度系数显示的精细化；
  
  More details displaying when analyzing.
* 盘面显示字体改为 **Arial**（原字体为 **Verdana**），并修正对齐不足的漏洞；
  
  Modify the font of legend and value from *Verdana* to *Arial*, and fixed the bug of alignment.
* 优化了一些代码的规范，例如部分使用的匿名内部类修改为 Lambda 表达式格式，例如 Hint 类型的难度系数的排序操作。
  
  Optimize some syntaxes of code, for example, using Lambda expression to replace anonymous inner class to sort difficulty ratings in *Hint* class.

### 删除功能

***Title: What's removed***

* 删除“退出应用”按钮。
  
  Because of *undo* button, *Quit-The-App* button is removed.



## 集成开发环境使用

***Title: IDE Using***

IntelliJ IDEA 社区版 2019.02 版本

IntelliJ IDEA 2019.02 Community



## 软件语言使用

***Title: Language Using***

编程语言使用：Java

Programming language using: Java

程序描述语言使用：英语

Language of contents using: English



## 代码版本

***Title: Version***

1.2.11（尚未修复 BUG 和 UL 技巧数组找不全的 bug……）

But I am too silly to fix the bug when finding BUG/UL with subset...


## 作者

***Title: Author***

小向（Sunnie）

