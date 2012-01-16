module Proj where
import System.IO -- open/save file
import System.Directory -- for checking if file exists
import Data.List.Split(splitOn) -- string splitting
import Data.Char -- isDigit and others


------------------------------------------------------------- data types -------------------------------------------------------------------
type ID = Int --row id
type Cell = String --Cell as string (but from the user point of view he can put integers here and they will be threated as integers in SUM/MUL/AVG functions)
data Row = MakeRow ID [Cell] deriving Show --Row with MakeRow constructor

class SheetTemplate s where -- sheet class 'template'
	-- 'methods' that needs to be implemented by derived classes: empty, getRows, setRows
	empty :: s
	getRows :: s -> [Row]
	setRows :: [Row] -> s -> s
	-- sets the value of the cell where params are:
	-- value, rowIndex, columnIndex, sheet where change will take place
	setValue :: Cell -> Int -> Int -> s -> s
	setValue val r c db = db' where
		db' = setRows os' db
		os' = 
			if doesRowExist (getRows db) r then 
				--if row exists we update its content by creating new row with old data and one new element (that we just set)
				-- and then we delete old row and replace it with our new
				addRowToSet (MakeRow r (insertAt (getCells (getRowWithId (getRows db) r)) val c)) 
					(removeRowWithId (getRows db) r) (getId (head (getRows db)))
			else 
				if length (getRows db) > 0 then
					--if sheet is not empty we add the new row to the set we search the set and find the correct place for new row
					--it may be at the end or in the middle so we search from head of rows
					addRowToSet (MakeRow r (insertAt [] val c)) (getRows db) (getId (head (getRows db)))
				else
					--if sheet is empty just add new row with the value inserted in desired column
					addRowToSet (MakeRow r (insertAt [] val c)) (getRows db) 0
	
	-- deletes a row from the set
	deleteRow :: Int -> s -> s
	deleteRow r sh = sh' where
		sh' = setRows os' sh
		os' = 
			if doesRowExist (getRows sh) r then 
				--delete row and decrease IDs of all rows with ID > deleted ID
				decreaseRowIdsAboveId (removeRowWithId (getRows sh) r) r
			else 
				--do nothing if row does not exists
				(getRows sh)
	
	--deleting a column is just deleting a value in each row at the columnIndex (ci) position (so cells list is decreased by 1)
	deleteColumn :: Int -> s -> s
	deleteColumn ci sh = sh' where
		sh' = setRows os' sh
		os' = removeInAllRows (getRows sh) ci
		removeInAllRows :: [Row] -> Int -> [Row]
		removeInAllRows [] _ = []
		removeInAllRows (x:xs) cIndex =
			MakeRow (getId x) (removeCellAtIndex (getCells x) cIndex) : removeInAllRows xs cIndex
					
data Sheet = Sh [Row] deriving Show -- this is out data type of sheet

instance SheetTemplate Sheet where --Sheet is child of SheetTemplate and if gives implementation to required 'methods'
	empty = Sh []
	getRows (Sh os) = os
	setRows os (Sh ps) = Sh os
	
	
----------------------------------------------------------- other functions -------------------------------------------------------------------	
--gets ID from the Row type
getId :: Row -> ID
getId (MakeRow i cells) = i

--removes the cell at 'index' position in the list of given cells
removeCellAtIndex [] _ = []
removeCellAtIndex (x:xs) index =
	if index == 0 then xs
	else
		x: removeCellAtIndex xs (index-1)

--decreases ID value of all rows which IDs are above provided Int value - used after delete row
decreaseRowIdsAboveId :: [Row] -> Int -> [Row]
decreaseRowIdsAboveId [] _ = []
decreaseRowIdsAboveId (x:xs) id =
	if getId x < id then x : decreaseRowIdsAboveId xs id
	else (MakeRow ((getId x)-1) (getCells x)) : decreaseRowIdsAboveId xs id

--gets [Cells] from the Row type
getCells :: Row -> [Cell]
getCells (MakeRow id cells) = cells

--inserts in the cells list desired cell value at the given position and returns new list of cells (it changes the value!)
insertAt :: [Cell] -> Cell -> Int -> [Cell]
insertAt [] val i = 
			if i==0 then [val]
			else emp : insertAt [] val (i-1)
insertAt (x:xs) val i =
			--if i==0 then val : x : xs  -- this way we add out value before existing (cells move one forward)
			if i==0 then val : xs -- this way we replace the cell content
			else x: insertAt xs val (i-1)

--checks if Row with given ID exists from the [Row]			
doesRowExist [] _ = False
doesRowExist (x:xs) id =
			if id == getId x then True
			else doesRowExist xs id

--returns Row with given ID from the [Row]			
getRowWithId :: [Row] -> Int -> Row
getRowWithId (x:xs) id =
			if id == getId x then x
			else getRowWithId xs id

--adds Row with given ID to the list of rows [Row]			
addRowToSet row [] currentId = 
			if getId row == currentId then [row]
			else (MakeRow currentId []) : addRowToSet row [] (currentId+1)
addRowToSet row (x:xs) currentId =
			if getId row > getId x then x : addRowToSet row xs (currentId+1)
			else row:x:xs

--removes row with given ID from the list of rows [Row]			
removeRowWithId :: [Row] -> Int -> [Row]
removeRowWithId (x:xs) id =
			if id == getId x then xs
			else x : removeRowWithId xs id

--function used to show rows on the screen
--shows given list of rows, param 'd' is the max cell width that should be used to show cells, originalRows are only for formula counting
showRows [] _ _ = return ("")
showRows (x:xs) originalRows d = do
	putStrLn ((show (getId x)) ++ (addSpacesAfterNum (getId x)) ++ " || " ++ (cellsToStringShow (getCells x) originalRows d) )
	showRows xs originalRows d

--helper function to create a gap between row number and 'table' border
addSpacesAfterNum rowNum =
	--in general it is not often to have 9999 rows and below this value left sheet border will be justified correctly
	if rowNum < 10 then
		"   "
	else if rowNum < 100 then
		"  "
	else if rowNum < 1000 then 
		" "
	else ""

--returns given Row cells (as [Cell]) as string line - used to show values on console
--the difference to cellsToString is that it sets their width to be shown well formatted	
cellsToStringShow [] _ _ = ""
cellsToStringShow (x:xs) originalRows d =
	if take 1 x == "=" then
		--remove "=" from beginning and count formula
		(centerString (countFormula (drop 1 x) originalRows) d) ++ columnsSeparator ++ (cellsToStringShow xs originalRows d)
	else
		(centerString x d) ++ columnsSeparator ++ (cellsToStringShow xs originalRows d)
  
--creates list of rows ([Row]) from a string - it is used to create rows from loaded file
createRowsFromString :: String -> [Row]
createRowsFromString line =
	createRowFromStringLines 0 (splitOn ("\n") (line))

--helper to create single [Row] from string
createRowFromStringLines :: Int -> [String] -> [Row]
createRowFromStringLines _ [] = [];
createRowFromStringLines id0 (x:xs)  = 
	if length x ==0 && xs == [] then [] --this prevents from creating new row from last empty line in file
	else
		(MakeRow id0 (splitOn (fileCellsSeparator) (x))) : createRowFromStringLines (id0+1) xs

--checks if given string starts with prefix
--params: prefix lenght (Int), prefix (String), string value (String)
startsWith :: Int -> String -> String -> Bool
startsWith prefixCount prefix string = 
	if take prefixCount string == prefix then True
	else False

--saves the given sheet to file named filename	
saveSheet sheet filename = 
	doWrite filename (take ((length (rowsToString (getRows sheet)))-1) (rowsToString (getRows sheet)))

--creates string from sheet rows - for saving purposes
rowsToString [] = ""
rowsToString (x:xs) = 
	(take ((length (cellsToString (getCells x)))- (length fileCellsSeparator)) (cellsToString (getCells x))) ++ "\n" ++ rowsToString xs

--returns given Row cells (as [Cell]) as string line (it adds separators between cells)
cellsToString [] = ""
cellsToString (x:xs) =
	x ++ fileCellsSeparator ++ (cellsToString xs)
	
--this function actually writes a file with given contents (as String)	
doWrite fname contents = 
	do 
	handle <- openFile fname WriteMode
	hPutStrLn handle contents
	hClose handle

--checks if string is a number
checkAllDigits :: String -> Bool
checkAllDigits [] = True
checkAllDigits (x:xs) = 
	if isDigit x then checkAllDigits xs
	else False
	
--checks if the given r,c (rowIndex,columnIndex) cell is in the formula string
checkIfContains :: String -> String -> String -> Bool
checkIfContains r c str = 
	if isThreeElemInString r fis c str then True
	else False

--checks if given String contains three one-char length Strings in the given order ex. isThreeElemInString "a" "b" "c" "vvabca" returns True
isThreeElemInString a b c str =
	if length str < 3 then False
	else threeElemInString a b c str

--helper function for isThreeElemInString - checks if given String contains three one-char length Strings in the given order
threeElemInString _ _ _ [] = False
threeElemInString a b c (x:y:z:xs) = 
	if (head a) == x && (head b) == y && (head c) == z then True
	else 
		--prevent from checking further when there is no more than 3 chars
		if xs == [] then False
		else
			threeElemInString a b c (y:z:xs)
	
--checks formula syntax (from String param)
checkFormulaSyntax :: String -> Bool
checkFormulaSyntax form =
	if startsWith 3 Proj.sum form || startsWith 3 Proj.mul form || startsWith 3 Proj.avg form then do
		let f = drop 4 form
		checkForm f
	else
		True

--checks the formula range - range as String i.e. 1,1;2,4;3,1-3,5;4,1
checkForm :: String -> Bool
checkForm s = 
	checkFormTab (splitOn fsplit s)

--helper for checkForm - it uses string splitted into [String]	i.e. from 1,1;2,4;3,1-3,5;4,1 we have ["1,1","2,4","3,1-3,5","4,1] as param
checkFormTab [] = True
checkFormTab (x:xs) =
	checkSym x && checkFormTab xs
	
--helper for checkForm - checks if single symbol is correct
checkSym sym = 
	if length (splitOn frangesplit sym) == 1 then
		-- this is case for 1,1
		checkSingle sym
	else if length (splitOn frangesplit sym) == 2 then
		-- this is case for 1,1-1,4 so what we need to do is to check both 1,1 is correct and 1,4 also
		if checkSingle (head (splitOn frangesplit sym)) && checkSingle (last (splitOn frangesplit sym)) then
			--after that we check if the given range is correct
			checkRangeType (head (splitOn frangesplit sym)) (last (splitOn frangesplit sym))
		else False
	else
		False
		
--we need to check if range is X,a-X,b or a,Y-b,Y
checkRangeType a0 a1 =
	if getInt 0 a0 == getInt 0 a1 || getInt 1 a0 == getInt 1 a1 then True
	else False

--check if single element is correct ex. symbol: "2,1"
checkSingle sym =
	if length (splitOn fis sym) == 2 then
		if checkAllDigits (getStr 0 sym) && checkAllDigits (getStr 1 sym) then True
		else False
	else
		False

--gets string value from i.e. "2,1", ex.: getStr 0 "2,1" returns "2" and getStr 1 "2,1" returns "1"
getStr i str = 
	if i==0 then head (splitOn fis str)
	else last (splitOn fis str)

--gets Int value from i.e. "2,1", ex.: getInt 0 "2,1" returns 2	and getInt 1 "2,1" returns 1
getInt i str = 
	read (getStr i str) :: Int

--gets max length of the cell value
getMaxCellStringLength [] = 0
getMaxCellStringLength rows =
	maximum (getCellsLengths rows)

--returns list of all cells values lengths	
getCellsLengths [] = []
getCellsLengths (x:xs) =
	getCellsLengthsFromRow (getCells x) ++ getCellsLengths xs

--returns list of all cells values lengths from one given row
getCellsLengthsFromRow [] = []
getCellsLengthsFromRow (x:xs) =
	length x : getCellsLengthsFromRow xs

--gets max index of the column in the sheet (column exists if there is/was a value in any row at the columnIndex)
getMaxColumnsCount [] = 0
getMaxColumnsCount rows = maximum (getColumnsCount rows)

--get list of number of columns in each row
getColumnsCount :: [Row] -> [Int]
getColumnsCount [] = []
getColumnsCount (x:xs) =
	getMaxColumnsCountFromRow x : getColumnsCount xs
	
--returns number of cells in the given row
getMaxColumnsCountFromRow :: Row -> Int
getMaxColumnsCountFromRow row = length (getCells row)

--prepares the top label with column indexes for showing to screen
prepareTopLabelNumbers currentCol maxColumnsCount maxCellLength =
	if maxColumnsCount > 0 then 
		(centerString (show currentCol) maxCellLength) ++ columnsSeparator ++ prepareTopLabelNumbers (currentCol+1) (maxColumnsCount-1) maxCellLength
	else
		""

--creates a string with given values repeated 'amount' times
createValues :: String -> Int -> String	
createValues val amount = 
	if amount > 0 then val ++ (createValues val (amount-1))
	else ""

--creates 'num' number of spaces as string
createSpaces num =
	createValues " " num

--centers string it works that way: the container is set to 'len' chars and the given 'str' has the same spaces amount on left and right side
centerString :: String -> Int -> String	
centerString str len = 
	if (length str) == len then str
	else
		createSpaces ((len - (length str)) `div` 2) ++ str ++ createSpaces ((len - (length str)) - ((len - (length str)) `div` 2))

---------------------------------- START: FORMULA COUNTING -----------------------
{-
--this method counts the formulas created by user it is complicated and long
--special formula format is i.e. SUM 1,1;2,4;3,1-3,5;4,1
countFormulas :: [Row] -> [Row] -> [Row]
countFormulas [] _ = []
countFormulas (x:xs) orgRows = countRow x orgRows : countFormulas xs orgRows
	
	where 
		--counts new data for the row
		countRow row (x:xs) = 
			if containsSpecial (getCells row) then
				--if row contains special formula then we need to count it and update row
				MakeRow (getId row) (updateRowWithSpecialCountedData (countSpecial (getSpecial (getCells row)) orgRows) (getCells row))
			else row --if no special formula found then just leave the row
		
		--updates row with counted formula data it takes new value and [Cell] as params
		updateRowWithSpecialCountedData val (x:xs) =
			-- we search the cells for special formula and replace it with the new value (counted formula value)
			if startsWith 3 Proj.sum x || startsWith 3 Proj.mul x || startsWith 3 Proj.avg x then
				val : xs
			else x : updateRowWithSpecialCountedData val xs
			
		--checks if Row as list of cells contains special formula
		containsSpecial [] = False
		containsSpecial (x:xs) = 
			if startsWith 3 Proj.sum x || startsWith 3 Proj.mul x || startsWith 3 Proj.avg x then True
			else containsSpecial xs
		
		--gets the special formula as string from the list of cells (firstly cells are checked if symbol really exists on the list)
		getSpecial [] = ""
		getSpecial (x:xs) = 
			if startsWith 3 Proj.sum x || startsWith 3 Proj.mul x || startsWith 3 Proj.avg x then x
			else getSpecial xs
-}

--it counts special formula, it takes formula as string, all sheet rows and returns new counted value as string to be saved into cell
--param formula example 1,1;2,4;3,1-3,5;4,1
countFormula :: String -> [Row] -> String
countFormula content orgRows = 
	if startsWith 3 Proj.sum content then
		--we compare count sum results to special Int to check if all rows are really Int (otherwise countSumFromCells returns this special Int value)
		if (countSumFromCells (analizeRange (drop 4 content)) orgRows) /= formulaerrorint then
			show (countSumFromCells (analizeRange (drop 4 content)) orgRows)
		else formulaerror
	else if startsWith 3 Proj.mul content then
		--we compare count sum results to special Int to check if all rows are really Int (otherwise countSumFromCells returns this special Int value)
		if (countMulFromCells (analizeRange (drop 4 content)) orgRows) /= formulaerrorint then
			show (countMulFromCells (analizeRange (drop 4 content)) orgRows)
		else formulaerror
	else if startsWith 3 Proj.avg content then
		--we compare count sum results to special Int to check if all rows are really Int (otherwise countSumFromCells returns this special Int value)
		if (countSumFromCells (analizeRange (drop 4 content)) orgRows) /= formulaerrorint then
			-- counting AVG (average) is just sum and divide by items count
			show ( (countSumFromCells (analizeRange (drop 4 content)) orgRows) `div` (length (splitOn fsplit (drop 4 content))) )
		else formulaerror
	else content	
	
	where
		
		--take table of cells indexes ie. ["2,1", "4,4"] and original rows from sheet to count their sum - returns formulaerrorint in any cell is NOT digit
		countSumFromCells [] _ = 0
		countSumFromCells (x:xs) orgRows = 
			if checkCellsAreDigits (x:xs) orgRows then
				(getCellValueStr x orgRows) + (countSumFromCells xs orgRows)
			else formulaerrorint

		--take table of cells indexes ie. ["2,1", "4,4"] and original rows from sheet and multiplies them - returns formulaerrorint in any cell is NOT digit
		countMulFromCells [] _ = 1
		countMulFromCells (x:xs) orgRows = 
			(getCellValueStr x orgRows) * (countSumFromCells xs orgRows)
		
		--checks if given row as [Cell] contains only digits
		checkCellsAreDigits [] _ = True
		checkCellsAreDigits (x:xs) orgRows = 
			checkCellDigit x orgRows && checkCellsAreDigits xs orgRows
		
		--check if given cell is a number, param is cell coord "rowIndex,columnIndex" as string ex. "2,1"
		checkCellDigit :: String -> [Row] -> Bool
		checkCellDigit coord orgRows = do
			let val = getCellValue (getInt 0 coord) (getInt 1 coord) orgRows orgRows
			checkAllDigits val == True
			
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

		--gets cell value as Int from a given indexes as string <row,col> ex. "2,1" gets Int value from cell at row 2 column 1
		--this function returns formulaerrorint Int value if required field is NOT a number
		getCellValueStr :: String -> [Row] -> Int
		getCellValueStr coord orgRows = do
			let val = getCellValue (getInt 0 coord) (getInt 1 coord) orgRows orgRows
			if checkAllDigits val == True then
				read (val) :: Int
			else formulaerrorint
			
		--gets cell value as string for a given r-rowIndex and c-columnIndex
		getCellValue _ _ [] _ = emp
		getCellValue r c (x:xs) orgRows = 
			if r == 0 then getValFromCells c (getCells x) orgRows
			else getCellValue (r-1) c xs orgRows
	
		
		
{-		
		cellsToStringShow (x:xs) originalRows d =
			if take 1 x == "=" then
				--remove "=" from beginning and count formula
				(centerString (countFormula (drop 1 x) originalRows) d) ++ columnsSeparator ++ (cellsToStringShow xs originalRows d)
			else
				(centerString x d) ++ columnsSeparator ++ (cellsToStringShow xs originalRows d)
				-}
				
--helper for getCellValue - returns value at c-columnIndex position from list of cells
getValFromCells _ [] _ = emp
getValFromCells c (x:xs) orgRows = 
			if c==0 then 
				if take 1 x == "=" then
					countFormula (drop 1 x) orgRows
				else x
			else getValFromCells (c-1) xs orgRows
			
---------------------------------- END: FORMULA COUNTING -----------------------------


--------------------------------------------------------------------- constants -------------------------------------------------------------------
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
--help in main menu
helpMain = "help - shows this help \n" ++
		"new <name> - creates new sheet with a given filename \n" ++
		"open <filename> - opens <filename> sheet \n" ++
		"exit - quits the program \n"
--help in loaded/new sheet menu
helpOpened = "help - shows this help \n " ++
		"open <filename> - opens <filename> sheet \n" ++
		"new <name> - creates new sheet with a given name \n" ++
		"show - prints current sheet to console \n" ++
		"save [<filename>] - saves current sheet, optionally to the given filename \n" ++
		"setval <rowIndex> <columnIndex> <value> - puts <value> to requested cell (updates value if exists) \n" ++
		"    <value> can be a SUM/MUL/AVG function i.e. SUM 1,1;3,5;2,0-2,5 \n" ++
		"    examples: setval 1 2 hello OR setval 3 1 SUM 1,1;3,5;2,0-2,5 \n" ++
		"del <rowIndex> <columnIndex> - deletes requested cell \n" ++
		"delrow <rowIndex> - deletes requested row \n" ++
		"delcol <columnIndex> - deletes requested column \n" ++
		"exit - quits the program \n"


----------------------------------------------------------------------- logic ---------------------------------------------------------------------
--starts program here
main = do
	putStrLn "Welcome to HaskCel"
	startLoop

--this is main menu
startLoop = do
	putStrLn "Type command (help - shows options):"
	putStr con --show prompt
	w <- getLine --gets user command
	if w=="exit" then do --exit
		putStrLn "Goodbye"
	----------------	
	else if w=="help" then do --prints help
		putStrLn helpMain
		startLoop --go back to the beginning of function
	----------------	
	else if (take 4 w) =="open" then do 
		let filename = (drop 5 w)
		if filename == [] then do
			hPutStrLn stderr "No arguments error"
			startLoop
		else do
			exists <- doesFileExist filename --check existance
			if exists == True then do
				putStrLn ("Opening file = " ++ filename)
				contents <- readFile filename
				let rows = createRowsFromString contents --make rows from file content
				let sheet1 = Sh rows --create Sheet from rows
				loadedSheet sheet1 filename --go to loadedSheet part (where we have Sheet created or loaded)
			else do
				hPutStrLn stderr ("File does not exists: " ++ filename)
				startLoop
	----------------			
	else if (take 3 w) =="new" then do 
		let args = (drop 4 w)
		if args == [] then do
			hPutStrLn stderr "No arguments error"
			startLoop
		else do
			let filename = args ++ ext
			putStrLn ("New file = " ++ filename)
			let sheet1 = empty --create new empty Sheet
			loadedSheet sheet1 filename  --go to loadedSheet part (where we have Sheet created or loaded)
	----------------		
	else do 
		hPutStrLn stderr "Unsupported command"
		startLoop

--this is part after loading/creating a sheet		
loadedSheet sheet name = do
	putStrLn ("<" ++ name ++ "> Type command (help - shows options):")
	putStr con --show prompt
	w <- getLine --gets user command
	if w=="exit" then do 
		putStrLn "Goodbye"
	----------------
	else if w=="help" then do --prints help
		putStrLn helpOpened
		loadedSheet sheet name 
	----------------
	else if (take 4 w) =="open" then do --opens file (same as in main menu)
		let filename = (drop 5 w)
		if filename == [] then do
			hPutStrLn stderr "No arguments error"
			loadedSheet sheet name
		else do
			exists <- doesFileExist filename 
			if exists == True then do
				putStrLn ("Opening file = " ++ filename)
				contents <- readFile filename
				let rows = createRowsFromString contents
				let sheet1 = Sh rows
				loadedSheet sheet1 filename  
			else do
				hPutStrLn stderr ("File does not exists: " ++ filename)
				loadedSheet sheet name
	----------------
	else if (take 4 w) =="save" then do --saves the Sheet (to current filename or to the one given as parameter)
		let filename = (drop 5 w)
		if(filename == []) then do
			if drop ((length name)-4) name == ext then do --check if name has extention
				putStrLn ("Save to file = " ++ name)
				saveSheet sheet name -- save to the same name
				loadedSheet sheet name 
			else do --add extension if filename does not have extension
				let newName = name ++ ext
				putStrLn ("Save to file = " ++ newName)
				saveSheet sheet newName -- save to the same name
				loadedSheet sheet newName 
		else do
			if drop ((length filename)-4) filename == ext then do --check if name has extention
				putStrLn ("Save to file = " ++ filename)
				saveSheet sheet filename -- save to given as parameter name
				loadedSheet sheet filename 
			else do --add extension if filename does not have extension
				let newName = filename ++ ext
				putStrLn ("Save to file = " ++ newName)
				saveSheet sheet newName -- save to given as parameter name
				loadedSheet sheet newName 
	----------------
	else if (take 3 w) =="new" then do --creates new Sheet (same as in main menu)
		let args = (drop 4 w)
		if args == [] then do
			hPutStrLn stderr "No arguments error"
			loadedSheet sheet name
		else do
			let filename = args ++ ext
			putStrLn ("New file = " ++ filename)
			let sheet1 = empty
			loadedSheet sheet1 filename  
	----------------
	else if w=="show" then do --shows the Sheet
		let rows = (getRows sheet)
		if length rows == 0 then do
			hPutStrLn stderr "Empty sheet"
			loadedSheet sheet name 
		else do
			let maxCellStringLength = getMaxCellStringLength rows --count max cell width
			let topLabel = "     || " ++ (prepareTopLabelNumbers 0 (getMaxColumnsCount rows) maxCellStringLength) --create top label with column indexes
			putStrLn topLabel
			putStrLn (createValues topLabelSeparator (length topLabel)) -- top Sheet border
			showRows rows rows maxCellStringLength -- prints rows
			loadedSheet sheet name 
	----------------
	else if (take 6 w) =="setval" then do --updates/inserts value
		let params = (drop 7 w)
		let rowIndex = takeWhile (/=' ') params
		let params2 = drop 1 (dropWhile (/=' ') params)
		let columnIndex = takeWhile (/=' ') params2
		let val = drop 1 (dropWhile (/=' ') params2)
		if checkAllDigits rowIndex && checkAllDigits columnIndex then do
			if checkIfContains rowIndex columnIndex val then do --check if there is special formula and if it used destination cell as source cell
				hPutStrLn stderr ("SUM/MUL/AVG formula incorrect (can't count formula with use of destination cell): " ++ val)
				loadedSheet sheet name 
			else do
				if checkFormulaSyntax val then do -- now check the syntax of the formula
					putStrLn ("Inserting value to current sheet: value=" ++ val ++ " at=" ++ rowIndex ++ "," ++ columnIndex)
					if startsWith 3 Proj.sum val || startsWith 3 Proj.mul val || startsWith 3 Proj.avg val then do
						--if the user puts formula in the cell it starts with "="
						let newsheet = setValue ("=" ++ val) (read rowIndex :: Int) (read columnIndex :: Int) sheet --set value in the cell
						loadedSheet newsheet name 
						--putStrLn ("Count formulas")
						--let s2 = Sh (countFormulas (getRows newsheet) (getRows newsheet))
						--loadedSheet s2 name 
					else do 
						let newsheet = setValue val (read rowIndex :: Int) (read columnIndex :: Int) sheet --set value in the cell
						loadedSheet newsheet name 
				else do
					hPutStrLn stderr ("SUM/MUL/AVG formula incorrect: " ++ val)
					loadedSheet sheet name 
		else do
			hPutStrLn stderr ("Invalid row/column value (not a number): row=" ++ rowIndex++", column="++columnIndex)
			loadedSheet sheet name 
	----------------
	else if (take 4 w) =="del " then do --deletes single cell
		let params = (drop 4 w)
		let rowIndex = takeWhile (/=' ') params
		let columnIndex = drop 1 (dropWhile (/=' ') params)
		if checkAllDigits rowIndex && checkAllDigits columnIndex then do
			putStrLn ("Remove value at=" ++ rowIndex ++ "," ++ columnIndex)
			--this is just setting cell value to empty - it does not move(change positions of) cells in the row!
			let newsheet = setValue emp (read rowIndex :: Int) (read columnIndex :: Int) sheet
			loadedSheet newsheet name 
		else do
			hPutStrLn stderr ("Invalid row/column value (not a number): row=" ++ rowIndex++", column="++columnIndex)
			loadedSheet sheet name 
	----------------
	else if (take 6 w) =="delrow" then do --deletes single row
		let rowIndex = (drop 7 w)
		if checkAllDigits rowIndex then do
			putStrLn ("Remove row " ++ rowIndex)
			let newsheet = deleteRow (read rowIndex :: Int) sheet
			loadedSheet newsheet name 
		else do
			hPutStrLn stderr ("Invalid row value (not a number): row=" ++ rowIndex)
			loadedSheet sheet name 
	----------------
	else if (take 6 w) =="delcol" then do --deletes single column
		let colIndex = (drop 7 w)
		if checkAllDigits colIndex then do
			putStrLn ("Remove column " ++ colIndex)
			let newsheet = deleteColumn (read colIndex :: Int) sheet
			loadedSheet newsheet name 
		else do
			hPutStrLn stderr ("Invalid column value (not a number): column=" ++ colIndex)
			loadedSheet sheet name 
	----------------
	else do 
		hPutStrLn stderr "Unsupported command"
		loadedSheet sheet name  



   

