package EE219Lab3

import spinal.core._
import spinal.lib._
import java.io.File
import java.io.PrintWriter

object Test {
  // Single test case if you required to check the wave
  // val cfgArray = Array(Conv2DConfig(imageC = 1, imageW = 3, imageH = 3, filterNum = 1, filterSize = 3))

  // Standard test cases
  val cfgArray = Array(
    Conv2DConfig(imageC = 1, imageW = 1, imageH = 1, filterNum = 1, filterSize = 3),
    Conv2DConfig(imageC = 1, imageW = 3, imageH = 3, filterNum = 3, filterSize = 3),
    Conv2DConfig(imageC = 1, imageW = 4, imageH = 3, filterNum = 7, filterSize = 1),
    Conv2DConfig(imageC = 3, imageW = 5, imageH = 3, filterNum = 2, filterSize = 3),
    Conv2DConfig(imageC = 3, imageW = 4, imageH = 5, filterNum = 3, filterSize = 3),
    Conv2DConfig(imageC = 3, imageW = 3, imageH = 3, filterNum = 4, filterSize = 1),
    Conv2DConfig(imageC = 3, imageW = 3, imageH = 1, filterNum = 6, filterSize = 1),
    Conv2DConfig(imageC = 3, imageW = 5, imageH = 3, filterNum = 7, filterSize = 1),
    Conv2DConfig(imageC = 8, imageW = 5, imageH = 4, filterNum = 4, filterSize = 3),
    Conv2DConfig(imageC = 8, imageW = 4, imageH = 5, filterNum = 4, filterSize = 3),
    Conv2DConfig(imageC = 8, imageW = 4, imageH = 3, filterNum = 7, filterSize = 3),
    Conv2DConfig(imageC = 8, imageW = 3, imageH = 4, filterNum = 7, filterSize = 1),
    Conv2DConfig(imageC = 16, imageW = 4, imageH = 1, filterNum = 1, filterSize = 1),
    Conv2DConfig(imageC = 16, imageW = 4, imageH = 3, filterNum = 7, filterSize = 1),
    Conv2DConfig(imageC = 16, imageW = 4, imageH = 4, filterNum = 7, filterSize = 1)
  )

  val correctArray = Array.fill(cfgArray.size)(false)

  // Convert image to string
  def strImage(img: Array[Array[Array[Int]]]) = {
    ("[\n" + img.map { row => "\t[" + row.map { ch => "[" + ch.map { n => f"$n%02X" }.mkString(",") + "]" }.mkString(",") + "]" }.mkString(",\n") + "\n]")
  }

  // Convert matrix to string (2-bits hex)
  def strMatrix2(mat: Array[Array[Int]]) = {
    ("[\n" + mat.map { row => "\t[" + row.map { n => f"$n%02X" }.mkString(",") + "]" }.mkString(",\n") + "\n]")
  }

  // Conver matrix to string (6-bits hex)
  def strMatrix6(mat: Array[Array[Int]]) = {
    ("[\n" + mat.map { row => "\t[" + row.map { n => f"$n%06X" }.mkString(",") + "]" }.mkString(",\n") + "\n]")
  }

  // Test if two matrix are equal
  def equalMatrix(mat1: Array[Array[Int]], mat2: Array[Array[Int]]) = {
    (mat1 zip mat2).map { case (r1, r2) => (r1 zip r2).map { case (e1, e2) => e1 == e2 }.reduce(_ & _) }.reduce(_ & _)
  }

  // Generate string of PASS and FAIL
  def resultText(result: Boolean) = { if (result) "PASS" else "FAIL" }

  // Check if test is pass and write the log
  def resultCheck(index: Int, cfg: Conv2DConfig, image: Array[Array[Array[Int]]], matWeight: Array[Array[Int]], matInput: Array[Array[Int]], matInputSim: Array[Array[Int]], matOutput: Array[Array[Int]], matOutputSim: Array[Array[Int]]) = {
    val file = new PrintWriter(new File("build/Test" + f"${index}%02d" + ".txt"))

    val correct = equalMatrix(matInput, matInputSim) && equalMatrix(matOutput, matOutputSim)
    correctArray(index) = correct

    println("@@@@@@@@@@@@@@@@@@")
    println("@@@@@ Test" + f"${index}%02d" + " @@@@@")
    println("@@@@@@ " + resultText(correct) + " @@@@@@")
    println("@@@@@@@@@@@@@@@@@@")

    file.println("@@@@@@@@@@@@@@@@@@")
    file.println("@@@@@ Test" + f"${index}%02d" + " @@@@@")
    file.println("@@@@@@ " + resultText(correct) + " @@@@@@")
    file.println("@@@@@@@@@@@@@@@@@@")
    file.println()

    file.println("imageC = " + cfg.imageC.toString)
    file.println("imageW = " + cfg.imageW.toString)
    file.println("imageH = " + cfg.imageH.toString)
    file.println("filterSize = " + cfg.filterSize.toString)
    file.println("filterNum = " + cfg.filterNum.toString)
    file.println("matrixM = " + cfg.matrixM.toString)
    file.println("matrixN = " + cfg.matrixN.toString)
    file.println()

    file.println("image")
    file.println(strImage(image))
    file.println()

    file.println("mat W")
    file.println(strMatrix2(matWeight))
    file.println()

    file.println("mat X (GT)")
    file.println(strMatrix2(matInput))
    file.println()

    file.println("mat X (HW)")
    file.println(strMatrix2(matInputSim))
    file.println()

    file.println("mat Y (GT)")
    file.println(strMatrix6(matOutput))
    file.println()

    file.println("mat Y (HW)")
    file.println(strMatrix6(matOutputSim))
    file.println()

    file.close()
  }

  // Summary the test result and write the log
  def resultSummary() = {
    val file = new PrintWriter(new File("build/Summary.txt"))

    val testTotal = correctArray.size
    val testPass = correctArray.count(_ == true)

    println("--------------------------------")
    println("######### TEST SUMMARY #########")
    println(f"PASS: [${testPass}%02d/${testTotal}%02d]")
    println("--------------------------------")

    file.println("--------------------------------")
    file.println("######### TEST SUMMARY #########")
    file.println(f"PASS: [${testPass}%02d/${testTotal}%02d]")
    file.println("--------------------------------")

    file.close()
  }
}
