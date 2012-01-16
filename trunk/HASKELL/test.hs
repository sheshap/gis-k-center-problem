module Proj where
import System.IO -- open/save file
import System.Directory -- for checking if file exists
import Data.List.Split(splitOn) -- string splitting
import Data.Char -- isDigit and others

type Cell = String
{-
checkIfDestinationCellUsedInOtherFormulaCells  _ _ [] = False	
checkIfDestinationCellUsedInOtherFormulaCells rowIndex columnIndex (x:xs) = 
	if startsWith 3 Proj.sum x || startsWith 3 Proj.mul x || startsWith 3 Proj.avg x then
		compareVals rowIndex columnIndex (analizeRange (drop 4 x)) || checkIfDestinationCellUsedInOtherFormulaCells rowIndex columnIndex xs
	else False

compareVals :: String -> String -> [String] -> Bool
compareVals _ _ [] = False
compareVals a0 a1 (x:xs) =
	if (a0 ++ fis ++ a1) == x then True
	else compareVals a0 a1 xs
-}

checkIfDestinationCellUsedInOtherFormula _ _ [] = False
checkIfDestinationCellUsedInOtherFormula rowIndex columnIndex (x:xs) = 
	checkIfDestinationCellUsedInOtherFormulaCells  rowIndex columnIndex x || 
		checkIfDestinationCellUsedInOtherFormula rowIndex columnIndex xs 
		
	
checkIfDestinationCellUsedInOtherFormulaCells  _ _ [] = False	
checkIfDestinationCellUsedInOtherFormulaCells rowIndex columnIndex (x:xs) = 
	if x /= [] && head x == '=' then
		compareVals rowIndex columnIndex (analizeRange (drop 5 x)) || checkIfDestinationCellUsedInOtherFormulaCells rowIndex columnIndex xs
	else checkIfDestinationCellUsedInOtherFormulaCells rowIndex columnIndex xs

compareVals :: String -> String -> [String] -> Bool
compareVals _ _ [] = False
compareVals a0 a1 (x:xs) = 
	if (a0 ++ fis ++ a1) == x then True
	else compareVals a0 a1 xs
	
	
	
--it takes formula values (part after SUM/MUL/AVG) and divides into list of cells that takes part in counting formula value
--param formula example 1,1;2,4;3,1-3,5;4,1
analizeRange :: String -> [String]
analizeRange ct = 
	analizeSplitted (splitOn fsplit ct)
	
--continues analizing formula, gets splitted formula values (split by fsplit)
--param formula example ["1,1","2,4","3,1-3,5","4,1"]
analizeSplitted [] = []
analizeSplitted (x:xs) =
	if takeWhile (/=frangesplitc) x == [] then
		-- single element ex. 1,2
		x : analizeSplitted xs
	else
		-- range element ex. 2,1-2,7 or 1,3-1,6
		splitRange (head (splitOn frangesplit x)) (last (splitOn frangesplit x)) ++ analizeSplitted xs

--splits the formula range into table of values ex. 2,1-2,4 to ["2,1","2,2","2,3","2,4"]
splitRange a0 a1 = 
	if takeWhile (/= fisc) a0 == takeWhile (/= fisc) a1 then
		splitIndex0 a0 a1
	else 
		splitIndex1 a0 a1
		
-- takes range start and end (row is constant) - range x,v x,u ex. 2,1 2,5	
splitIndex0 a0 a1 = 
			a0 : index0 a0  ((getInt 1 a1) - (getInt 1 a0))
		
-- takes range start and end (column is constant) - range v,y u,y ex. 1,3 4,3	
splitIndex1 a0 a1 =
	a0 : index1 a0  ((getInt 0 a1) - (getInt 0 a0))

--creates middle elements with count 'range' (by index0 - so increments rows) i.e. index0 2,1 4 creates table ["2,1","2,2","2,3","2,4"]
index0 a0 range =
	if range > 0 then
		((getStr 0 a0)  ++ fis ++ ( inc (getStr 1 a0) ))
		 : index0 ((getStr 0 a0)  ++ fis ++ ( inc (getStr 1 a0) )) (range-1)
	else []
	--creates middle elements with count 'range' (by index1 - so increments columns) i.e. index1 2,1 4 creates table ["2,1","3,1","4,1","5,1"]
index1 a0 range =
	if range > 0 then
		(inc (getStr 0 a0)  ++ fis ++ (getStr 1 a0))
		 : index1 ( (inc (getStr 0 a0))  ++ fis ++ (getStr 1 a0) ) (range-1)
		else []

--increments string value by 1 (this MUST be string value that is parseable to Int)
inc str = show (1 + (read str :: Int))


--gets string value from i.e. "2,1", ex.: getStr 0 "2,1" returns "2" and getStr 1 "2,1" returns "1"
getStr i str = 
	if i==0 then head (splitOn fis str)
	else last (splitOn fis str)

--gets Int value from i.e. "2,1", ex.: getInt 0 "2,1" returns 2	and getInt 1 "2,1" returns 1
getInt i str = 
	read (getStr i str) :: Int
	
	
-- sheet files default extension
ext = ".she"
-- console sign (prompt)
con = "HaskCel>> "
-- empty string for sheet cell
emp = ""
-- sum function in the cell
sum = "SUM"
-- multiply function in the cell
mul = "MUL"
-- average function in the cell
avg = "AVG"
-- functions arguments splitter
fsplit = ";"
-- split between ranges
frangesplit = "-"
-- split between ranges as char
frangesplitc = '-'
-- function indexes split - split between cell indexes
fis = ","
-- function indexes split as char - split between cell indexes
fisc = ','
--separator in file
fileCellsSeparator = "<@#x$%>"
-- error in formula
formulaerror = "###"
-- when error in formula - we return special flag value
minInt = minBound::Int
formulaerrorint = (minInt + 2)
-- columns separator
columnsSeparator = "   "
-- top label separator
topLabelSeparator = "="


--checks if given string starts with prefix
--params: prefix lenght (Int), prefix (String), string value (String)
startsWith :: Int -> String -> String -> Bool
startsWith prefixCount prefix string = 
	if take prefixCount string == prefix then True
	else False
	
	
	
--inserts in the cells list desired cell value at the given position and returns new list of cells (it changes the value!)
insertAt :: [Cell] -> Cell -> Int -> [Cell]
insertAt [] val i = 
			if i==0 then [val]
			else emp : insertAt [] val (i-1)
insertAt (x:xs) val i =
			--if i==0 then val : x : xs  -- this way we add out value before existing (cells move one forward)
			if i==0 then val : xs -- this way we replace the cell content
			else x : insertAt xs val (i-1)