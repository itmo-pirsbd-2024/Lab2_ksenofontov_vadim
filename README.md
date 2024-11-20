# Лабораторная работа № 2

## Раздел
Работа с памятью в Java


## Описание
В этой работе студентам предстоит реализовать одну из изученных ранее структур данных поиска с применением внешней памяти для Java, а также провести анализ производительности и оптимизацию, если потребуется.


## Характеристика структуры данных
(https://neerc.ifmo.ru/wiki/index.php?title=Расширяемое_хеширование)

В рамках данной работы было реализовано расширяемое хэширование. 

При интенсивном добавлении значений в хеш-таблицу возникнает ситуация, когда хеш-таблица становится полностью заполненной и требуется перераспределить значения в ней. При больших размерах хеш-таблицы, не позволяющих хранить ее в памяти, это требует большого количества времени, и перезаписи больших объемов памяти на диске. Структура данных, позволяющая минимизировать перезапись объектов в памяти называется "Расширяемое хэширование".

Метод расширяемого хеширования (extendible hashing) заключается в том, что хеш-таблица представлена как каталог (directories), содержащий указатели на ячейки (buckets) которые имеют определенную вместимость. Наиболее эффективной с позиции работы с диском считается емкость, размер которой будет кратен единице дискового чтения/записи. Сама хеш-таблица будет иметь глобальную глубину (global depth), а каждая из ячеек имеет локальную глубину (local depth). Глобальная глубина показывает сколько последних бит будут использоваться для того чтобы определить, в какую емкость следует заносить значения. А из разницы локальной и глобальной глубины можно понять сколько указателей каталога ссылаются на ячейку. Эта связь описывается формулой 
```math
K = 2^{G - L}
```
, где G — глобальная глубина, L — локальная глубина, а K — количество ссылок на ячейку. Для поиска ячейки по директориям может использоваться цифровое дерево.

При добавлении в хэш-таблицу некоторого значения алгоритм предусматривает выполнение слудующих шагов:

- Перевод значения в двоичный вид, определение по последним G битам ячейки, в которую отправится значение.
- Если ячейка имеет свободное место, то значение помещается туда;
- Если ячейка переполнена, то в зависимости от ее локальной глубины:
  - Если она меньше чем глобальная глубина, значит на ячейку есть несколько указателей и достаточно перераспределить ее, разделив на две и занести значения в новые ячйки, увеличив у этих ячеек локальную глубину на 1.
  - Если же локальная глубина была равна глобальной то глобальная глубина увеличивается на 1, при этом удваивается количество ячеек, количество директорий, а также увеличивается на 1 количество последних бит по которым мы определяется подходящая значению ячейка.
  - Далее локальная глубина переполненной ячейки становится меньше глобальной и происходит описанное на соседней условной ветви разделение ячейки ее на две и так далее.

Рассмотрим пример вставки трех значений **9, 20, 26** в изначально содержащую значения таблицу с емкостью ячеек 3, представленную на рисунке:

| ![FirstStep1](https://github.com/user-attachments/assets/55b31d9b-e0bb-4ac2-a4a2-f6c605e0ffff)|
|-|

Для наглядности в качестве хэша от значений берутся сами эти значения. Значение **9** просто вставляется в соответствующую ячейку, так как в ней есть место:

| ![SecondStep1](https://github.com/user-attachments/assets/6c6dd2c2-aa74-4e20-9831-e849e045bd95)|
|-|

Вставка значения **20** потребует удвоения массива директорий, так как соответствующая ячейка **00** заполнена, а ее локальная степень равна глобальной. Значения из ячейки будут распределены на две новых под номерами **000** и **100**:

| ![ThirdStep1](https://github.com/user-attachments/assets/cc5e84ba-4dc8-4d08-89c8-70923b5e68f6)|
|-|

Вставка значения **26** потребует разбиения соответствующей ячейки **010** так как она заполнена, а ее локальная степень меньше глобальной. Значения из ячейки будут распределены на две новых под номерами **010** и **110**:

| ![ForthStep1](https://github.com/user-attachments/assets/a640316c-cc71-4ad1-8b8c-5d533ecf7393)|
|-|

## Реализация
### Интерфейсы
При разработке структуры данных применялся модульный подход, предусматривающий возможность замены отдельных механизмов альтернативными. Для их совместимости были реализованы классы-интерфейсы ``IHasher`` и ``IBucketManager``. В этих классах задаются методы для генерации хэшей по значению и, соответственно, для управления ячейками.  
### Хэш-функция
Для минимизации коллизий важно выбрать хэш-функцию, обеспечивающую максимальную равномерность распределения значений. От этого будет зависить количество коллизий в хэш-таблице.
В рамках работы было реализовано три хэширующих алгоритма:  
- **Дробное хэширование** Реализовано в классе ``FractionalHasher``. Хэш значение получается из исходного путем подстановки его в формулу $`[x * A] * maxHash`$, где ``x`` - исходное значение, ``A`` - произвольное действительное число от 0 до 1, ``[]`` - операция взятия дробной части, ``maxHash`` - макимально возможное значение хэш-функции. В работе были использованы различные значения ``A`` и ``maxHash = Integer.MAX_VALUE``.
- **Хэширование Guava** Реализовано в классе ``GuavaHasher`` на основе библиотек Google Guava. Хэш значение получается из исходного путем выполнения для него кода ```value ^= (value >>> 20) ^ (value >>> 12);
        return (value ^ (value >>> 7) ^ (value >>> 4));```, где ``value`` - исходное значение, ``>>>`` - сдвига вправо с дополнением нулями.
- **Универсальное хэширование** Реализовано в классе ``UniversalHasher``. Хэш значение получается из исходного путем подстановки его в формулу $``((a * x + b) \bmod p) \bmod maxHash``$, где ``x`` - исходное значение, ``p`` - произвольное простое число гарантированно большее, чем любое входное значение, ``a`` - натуральное число, меньшее чем p, ``b`` - ноль или натуральное число, меньшее чем p, ``maxHash`` - макимально возможное значение хэш-функции. В работе были использованы значения ``p = 10000000319``, ``a = 598653``, ``b = 3745213`` и ``maxHash = Integer.MAX_VALUE``.

За генерацию хэшей для конкретных значений отвечает переопределение интерфейсного метода ```generateHash()```.

### Ячейки
В классе ``Bucket`` в переменной ``bucketSize`` содержится информация о вместимости ``LinkedList-а``, в котором хранятся значения. Список был выбран для обеспечения константной сложноссти вставок и удалений.

### Менеджер ячеек
С учетом потенциальной разнородности систем внешнего хранения ячеек и, соответсвенно потенциальным обилием кода для взаимодействия с ними, менеджер ячеек, отвечающий за выделение ячеек и доступ к ним, был реализован в виде интерфейса и классов с его определениями. 
Методы инетрфейса ``IBucketManager``:
- ``long allocateNewBucket()`` - выделение новой ячейки с возвращением в вызывающую функцию ее id;
- ``Bucket getBucketById(long bucketId)`` - получение ячеки из системы хранения по ее id;
- ``void clear()`` - финальное взаимодействие с системами хранения ячеек (закрытие потоков вывода);

Было реализовано два менеджера - **Inmemory manager**, простой в реализации и сохраняющий все ячеки в памяти и служащий больше для проверки корректности реализации других менеджеров, и **Ondisk manager**, реализующий сохранение ячеек на диске и чтение их с него.
#### Inmemory manager
Класс ``InmemoryBucketManager`` релизует добавление ячеек в ``ArrayList``, и доступ к ним по индексам.

#### Ondisk manager
**Кэш.** Взаимодействие менеджера с диском происходило через подсистему кэша, хранящую некоторое количество ячеек в памяти, добавляющую их в кэш с диска и выгружающую их на диск, реализованную в классе ``BucketCache``. Сами ячейки хранятся в кэше в хэш-таблице в качестве значений, ключами к ним выступают их id. Изначально в качесве структуры данных для управления добавляемыми и удаляемыми из кэша ячейками планировалось использовать очередь, в которой хранились бы id ячеек. Как только очередь стала бы содержать предельное количество id ячеек (а соответствующие им ячейки были бы сохранены в хэш-таблице), следующее добавление в кэш не имеющейся в нем ячейки привело бы к удалению из очереди id, а из хэш таблицы - ячейки, добавленных в кэш раньше всех. Данное решение мотивировалось тем, что в отличие от B-дерева, у которого вершины, расположенные ближе к корню, используются более часто, у хэш таблицы вероятность того, что произойдет обращение к конкретной ячейке одинаково для всех ячеек, таким образом, отсутствует приоритет одной вершины над другой.

В действительности возможна ситуация, при которой перед аллокацией новой ячейки для переноса в нее части значений из другой, эта другая ячейка находится в кэше, но является первой в очереди на удаление. Аллокация новой ячейки приведет к удалению старой из кэша с записью ее на диск в состоянии, когда из нее не были удалены элементы, перемещенные в новую ячейку. Это приведет структуру данных в несогласованное состояние.

С учетом приведенных рассуждений был реализован подход с применением очереди с приоритетом. Теперь для каждой ячейки в хэш таблице ``bucketsPriorities`` хранится ее приоритет, который становится равным максимальному текущему приоритету + 1 всякий раз, когда данная ячейка оказывается востребованной. В случае вставки новых ячеек при наличии в кэше предельного количества ячеек из него удаляется ячейка с наименьшим приоритетом. Для сравнения целочисленных id ячеек при поддержке пирамиды внутри очереди, был реализован компаратор ``BucketPriorityComparator``, сравнивающий соответствующие id приоритеты.

Класс ``BucketCache`` содержит следующие методы:
- ``Bucket getBucketById(long bucketId)`` - получение ячейки из кэша по ее id. Если ячейка на содержалась в кэше, она добавляется туда с максимальным приоритетом;
- ``void addBucketInCache(long bucketId, Bucket bucket)`` - ячейка добавляется в кэш с максимальным приоритетом. В случае заполеннности из кэша удаляется ячейка с наименьшим приоритетом и сохраняется на диск;

**Емкость ячейки.** Наиболее эффективной с позиции работы с диском считается емкость, размер которой будет кратен единице дискового чтения/записи. На системе Windows для определения размера этой единицы можно посмотреть, до какого объема дополняется размер файла на диске:
|![image](https://github.com/user-attachments/assets/1cb1178e-5fe0-4016-b5dc-023245028d5c)|![image](https://github.com/user-attachments/assets/d6b43997-3401-4a8d-ac61-93a24ae26be2)|
|-|-|

Таким образом, на данной системе объем единицы дисковых чтения/записи равен **4 Кб**.

На системе Linux узнать это значение можно с помощью команд ``sudo hdparm -I /dev/sda | grep -i physical`` и ``lsblk -o NAME,PHY-SeC``:

|![image](https://github.com/user-attachments/assets/26461e73-d4cf-4eee-aba8-3249411fcaa0)|
|-|

На данной системе объем единицы дисковых чтения/записи равен **512 байтов**.

**Сериализация и десериализация ячеек.** Вывод и чтение ячеек с диска осуществлялся с помощью класса ``java.io.RandomAccessFile``, предоставляющего возможность перезаписи и чтения конкретных фрагментов файла. Поскольку размер каждой ячейки является фиксированным, ее перезапись не может расширить необходимое для нее место на диске. С учетом этого, доступ к каждой ячейке на диске осуществляется через установку файлового указателя на позицию, равную произведению id ячейки на размер сериализованной ячейки в байтах. Это работает за счет того, что генерация id ячеек происходит последовательно.
В Java реализован сериализатор для примитивных типов. Однако он при сериализации осуществляет сжатие, что приводит к непредсказуемости размеров итоговых байт-последовательностей. В связи с этим в рамках работы были реализованы собственные методы ``serializeBucket()`` и ``deserializeBucket()``для сериализации и десериализации ячеек. ``serializeBucket()`` отображает в первые 8 байтов два 4-байтных значения: текущее количество значений в ячейке и текущую локальную степень ячейки. Далее добавляются байты всех существующих в ячейке значений. В заключение байт-последовательность дополняется до фиксированного размера нулями. Десериализация для получения объектов ячеек из байт-последовательностей осуществляется с помощью обратного алгоритма. Для манипулирования байтами используется класс ``java.nio.ByteBuffer``.

### Реализация директорий
**Массив.** Простейшим способом реализации директорий является массив, в котороом содержатся директории с индексами от нуля до $`2^G`$. При таком подходе, как было сказано ранее, из разницы локальной и глобальной глубины можно понять сколько директорий ссылаются на ячейку. При дроблении ячейки на две происходит перезапись половины указателей на исходную ячейку. Перезаписываются те указатели, для которых индекс директории содержит единицу в бите соответствующем локальной степени. При таком подходе потенциально возможен слишком интенсивный рост массива директорий. Подход с массивом реализован в классе ``ExtendibleHashTable``.

**Хэш-таблица.** Проблема с потенциальным размером массива директорий может быть решена с помощью использования для поиска соответствующей ячейки цифрового дерева (trie). При реализации такого подхода директории представляют собой хэш-таблицу, содержащую указатели не на все возможные с учетом глобальной степени битовые остатки, а только на существующие. При этом при увеличении глобальной степени не происходит удвоение таблицы директорий. Добавляться необходимые директории будут по одной. Поиск соответствующей значению ячейки происходит путем итеративного деления на два модуя, по которому ищется в таблице битовый остаток, до тех пор, пока не будет найдено совпадение в таблице с директориями. Данный подход реализован в классе ``ExtendibleHashTable2``.

### Массив для элементов, создающих протяженные цепочки коллизий
В определенных условиях может возникнуть ситуация, при которой хэш-функция будет выдавать неравномерные значения, вследствие которых в одной ячейке, несмотря на дробления будет оказываться предельно возможное количество значений, не позволяющее осуществить новую вставку. Таким образом, дробление будет осуществляться пока не переполнится тип ``long``, сдвиг в котором будет осуществляться на величину, большую чем 62, или пока в куче не закончится место для хранения директорий. Для хранения значений, требующих разбиения ячеек, чья локальная степень равна 62 была создана отдельная хранящаяся в памяти хэш-таблица.

## Тестирование
### Сценарии тестирования
#### Хэш-функции
- Для тестирования качества функций хэширований каждой из них были сгенерированы хэши для **20'000'000** случайных значений и оценена их уникальность.

#### Ondisk manager
- Для начальной оценки успешности реализации компонента ``OndiskBucketManager`` содержимое файла на диске при его использовании сравнивалось с содержимым данных в памяти при использовании ``InmemoryBucketManager`` для одних и тех же данных.

#### Структура данных в целом
- Для качественного и количественного тестирования, соответственно, корректности работы и производительности реализации структуры данных в нее осуществлялась вставка итеративно возрастающего количества значений, после чего поиск каждого вставленного, далее осуществлялось удаление каждого значения и в завершение - повторный поиск всех вставденных ранее и впоследствии удаленных значений с расчетом на их отсутствие.
- Во избежание потенциальных искажений относительно реального сценария использования, вносимых тем, что значения ищутся и удаляются в том же порядке, в каком и вставлялись, генерация последовательности псевдослучайных чисел была реализована с усложнением. Цельная последовательность с единственным значением инициализации была заменена на конкатенацию нескольких меньших последовательностей, каждая из которых имела свое инициализирующее значение. Это позволило, генерируя перестановки массива с инициализирующими значениями, после чего осуществляя перестановку самой текущей подпоследовательности (хранение которой в памяти допустимо в силу небольшого размера) добиться изменения порядка поиска элементов в таблице и удаления из нее.
- Для комплексной оценки производительности стуркутуры данных при ее запусках с разными настраиваемыми параметрами были замерены различные характеристики:
  - Количество втавок элементов за миллисекунду;
  - Количество поисков элементов за миллисекунду;
  - Количество удалений элементов за миллисекунду;
  - Количество проверок на отсутсвие элементов за миллисекунду;
  - Скорость в Mb/s с позиции суммарного объема вставленных данных;
  - Скорость записи в Mb/s с позиции суммарного объема всех операций записи за все время вставки;
  - Скорость чтения в Mb/s с позиции суммарного объема всех операций чтения за все время вставки;
  - Размер массива с директориями;
  - Количесвто перемещений элементов (при вставке);
  - Максимальная зафиксированная локальная степень;
  - Максимальный Id ячейки;
  - Средняя наполненность ячеек;
  - Количство элементов в таблице для элементов, создающих коллизии;
  - Скорость записи в Mb/s с позиции суммарного объема всех операций записи за все время записи;
  - Скорость чтения в Mb/s с позиции суммарного объема всех операций чтения за все время чтения;
  - Время работы с очередью с приоритетами, обеспечивающей работу кэша. 

#### Кэш
- Для тестирования влияния размера кэша на производительность структуры данных были осуществлены запуски, при работе которых были замерены описанные выше характеристики при работе с **1_000_000** значений.

#### Директории
- Для тестирования влияния реализации директорий на производительность структуры данных были осуществлены запуски, при работе которых были замерены описанные выше характеристики при работе с **100_000** значений.
  
### Гипотезы
- Ожидается, что даже в случае реализации директорий в виде массива, но выборе при этом качественной с позиции числа коллизий хэш-функции, разрастание массива директорий не будет безмерным.  
- Ожидается, что универсальное хэширование продемнонстрирует наибольшую уникальность генерируемых хэш-значений.
- Вставка, поиск и удаление элементов из структуры данных будут осуществляться за константное время, то есть не будут зависеть от размера таблицы.
- Несмотря на то, что система Linux, на которой происходит тестирование информирует о том, что ее единица чтения/записи составляет **512 байтов**, более эффективной ожидается настройка ячеек с расчетом на размер единицы в **4 Кб**, так как это величина единицы чтения/записи на хост-системе, на которой Linux запускается в качестве виртуальной машины.
- Наращивание максимальной вместимости кэша ячеек не улучшит производительность структуры данных, так как ячейки в любом случае будут удаляться из кэша, просто позже на то количество затребований из кэша, на какое количество ячеек кэш стал более вместительным, в то время как из специфики структуры данных следует, что объем этого кэша не может быть сопоставим с размером всей структуры данных.  
- Скорость работы с диском ожидается в 2-3 раза более низкой чем пиковая скорость чтения и записи на носитель при рандомизированном доступе. Эта скорость для используемого носителя была измерена и составила примерно **200 Мб/c**. Снижению скорости относительно пиковой поспособствует выполнение программой помимо дисковых операций других вычислительных действий и наличие слоя абстракций, предоставляемого классом ``java.io.RandomAccessFile``.
 
|![image](https://github.com/user-attachments/assets/a6f0abfb-937f-404f-9795-63805ad1b51d)|![image](https://github.com/user-attachments/assets/5a619a62-519e-4a8a-be3c-53c0b9929244)|
|-|-|
  
### Результаты
#### Ondisk manager
Сравнение содержимого файла на диске при использовании ``OndiskBucketManager`` с содержимым данных в памяти при использовании ``InmemoryBucketManager`` для одних и тех же данных показало совпадение локальной степени, количества значений и самих значений.

|![2024-11-01_16-57-20](https://github.com/user-attachments/assets/03bb0f54-b7a1-41db-9950-dd38670c2705)|
|-|

#### Хэш-функции
Хэширование | Дробное, A = 0.66667 | Дробное, A = 0.0066667 | Дробное, A = 0.000066667 | Дробное, A = 0.0000066667 | Guava | Универсальное
--- | --- | --- | --- | --- | --- | ---
Доля уникальных хэшей | 0.0256296 | 0.6354638 | 0.99044865 | 0.99529115 | 0.99530255 | 0.9953292

Как и ожидалось, универсальное хэширование показало лучший результат по сравнению с остальными хэш-функциями. Однако увиденное преимущество едва заметно. Предполагается, что использование универсального хэширования сделает структуру данных более производительной. 

#### Структура данных в целом
Путем сопоставления измеренных значений отдельных характеристик работы структуры данных было проанализировано влияние использования конкретных модульных компонентов или установки в тот или иной параметр конкретного значения на производительность структуры данных.

**Хэш-функция**
Hasher | insertions, el/s | findings, el/s | deletions, el/s | deleted findings, el/s| size of directories | elements movings | max local depth | avg bucket filling | size of high collised values
--- | --- | --- |--- |--- |--- | --- | --- |--- |---
Fractional (A = 0.066667) | 74.8458 | 82.6696 | 98.0805 | 120.819 | 25921 | 816720 | 26 | 0.612332 | 0
Fractional (A = 0.0000066667) | 131.039 | 148.408 | 165.723 | 193.815 | 22485 | 708097 | 15 | 0.705905 | 0
Guava | 145.795 | 155.561 | 165.627 | 178.046 | 22455 | 708114 | 15 | 0.706848 | 0
Universal | 140.117 | 159.364 | 153.757 | 177.398 | 22537 | 710161 | 15 | 0.704276 | 0

Как и ожидалось, использование хэш-функций с меньшим количеством коллизий значительно повышает производительность операций вставки/поиска/удаления. Использование хэш-функции с большим количеством коллизий провоцирует более частое дробление ячеек ввиду их быстрого заполнения, в результате чего разрастается массив директорий.

**Генерация псевдослучайной последовательности**

Random generation | insertionsPerMs, el/s | findingsPerMs, el/s | deletionsPerMs, el/s | deletedFindingsPerMs, el/s
--- | --- | --- | --- |---
Стандартная | 157.245 | 177.871 | 184.663 | 204.098
Усложненная | 156.573 | 167.213 | 180.264 | 208.498

Результаты замеров скорости операций вставки/поиска/удаления показывают, что поиск и удаление значений из структуры данных в том же порядке, в каком они были добавлены не сильно отличается по производительности от реальных сценариев, когда этот порядок меняется.  

**Кэш**

Cache capacity | size of directories | time of insertionInMs, ms | cache priority queue time delta, ms | insertions per ms, el/s | findings per ms, el/s | deletions per ms, el/s | deleted findings per ms, el/s
--- | --- | --- | --- |--- |--- |--- |---
2 | 22537 | 9434.6 | 249.8 | 124.189 | 148.811 | 163.287 | 175.574
20 | 22537 | 8167.2 | 575.8 | 132.581 | 144.324 | 136.478 | 162.354
200 | 22537 | 10271.2 | 1559.2 | 102.336 | 111.522 | 129.471 | 140.294
2000 | 22537 | 29744.8 | 16763.4 | 35.1874 | 99.989 | 118.785 | 144.58

Постепенное изменение размера кэша от 0.01% до 10% от всего количества ячеек показало, что наращивание кэша только ухудшает производительность операций над структурой данных, так как время работы с очередью с приоритетами занимает все большую и большую часть от всего времени выполнения операции.

**Производительность операций вставки/поиска/удаления при разной емкости ячеек и для разного количества элементов**

Содержащиеся в ячейках представленной ниже таблицы значения представляют собой производительность операций вставки/поиска/удаления/проверки удаления для соответствующих значений емкости ячейки и количества элементов.

Size of bucket \ Number of elements | 8192 | 32768 | 131072 | 524288 | 2097152 | 8388608
--- | --- | --- | --- |--- |--- |---
32 | 70, 157, 153, 162 | 111, 205, 205, 215 | 173, 232, 239, 244 | 141, 198, 195, 207 | 129, 186, 195, 208 | 105, 148, 151, 157 |
64 | 186, 244, 213, 243 | 186, 223, 231, 244 | 187, 244, 247, 261 | 164, 203, 204, 212 | 179, 222, 220, 231 | 148, 161, 171, 176 |
128 | 138, 199, 199, 222 | 244, 278, 288, 269 | 199, 226, 231, 242 | 192, 200, 196, 206 | 188, 214, 221, 227 | 158, 176, 173, 175 |
256 | 244, 306, 308, 307 | 216, 237, 254, 266 | 203, 222, 228, 242 | 179, 190, 195, 207 | 193, 209, 217, 232 | 162, 191, 197, 202 |
512 | 202, 227, 250, 273 | 184, 212, 223, 257 | 191, 208, 223, 249 | 167, 182, 192, 214 | 185, 198, 204, 226 | 153, 173, 173, 187 |
1024 | 58, 55, 77, 72 | 201, 201, 221, 245 | 161, 171, 189, 224 | 174, 192, 210, 251 | 151, 164, 182, 214 | 142, 143, 170, 205 |
2048 | 17, 21, 25, 44 | 122, 129, 155, 205 | 127, 138, 163, 216 | 129, 140, 158, 210 | 127, 136, 158, 196 | 117, 118, 144, 190 |
4096 | 27, 10, 35, 65 | 100, 104, 132, 204 | 87, 94, 122, 202 | 89, 95, 115, 179 | 87, 83, 102, 152 | 68, 61, 82, 152 |
8192 | 86, 71, 84, 221 | 65, 62, 86, 164 | 66, 68, 93, 163 | 55, 59, 78, 145 | 51, 43, 50, 78 | 37, 40, 56, 110 |
16384 | 68, 59, 82, 185 | 39, 37, 54, 115 | 29, 29, 39, 90 | 30, 31, 43, 93 | 28, 23, 29, 49 | 23, 24, 34, 79 |

Анализ представленных в таблице результатов измерения позволяет сделать вывод, что скорость операций для конретного объема ячеек при различных количествах вставляемых в структуру данных элементов незначительно, но стабильно снижается. Оптимальным объемом ячейки в байтах с учетом результатов измерений можно считать диапазон от **128** до **512**.

**Производительность записи на диск в процессе операций вставки/поиска/удаления при разной емкости ячеек и для разного количества элементов**

Содержащиеся в ячейках представленной ниже таблицы значения представляют собой:
- скорость в Мб/с записи данных в структуру;
- суммарный размер в Кб всех данных, выводимых на диск за время вставки значений в структуру;
- скорость в Мб/с записи данных на диск по отношению ко всему времени вставки элементов в структуру;
- скорость в Мб/с записи данных на диск по отношению ко времени выполнения операций записи;

для соответствующих значений емкости ячейки и количества элементов.

Size of bucket \ Number of elements | 8192 | 32768 | 131072 | 524288 | 2097152 | 8388608
--- | --- | --- | --- |--- |--- |---
32 | 0.814589, 378, 3, 9 | 1.27819, 1515, 5, 9 | 2.00143, 6066, 8, 15 | 1.62257, 24262, 7, 13 | 1.49487, 97057, 6, 12 | 1.21176, 388104, 5, 14 |
64 | 2.1233, 612, 14, 30 | 2.14489, 2463, 14, 26 | 2.1607, 9875, 14, 27 | 1.89647, 39520, 13, 24 | 2.06226, 158075, 14, 27 | 1.70202, 632292, 11, 30 |
128 | 1.62588, 1107, 19, 52 | 2.84617, 4471, 34, 62 | 2.31608, 17950, 28, 54 | 2.22974, 71853, 27, 51 | 2.18692, 287495, 26, 53 | 1.83368, 1.15002e+06, 22, 58 |
256 | 2.82422, 2075, 63, 125 | 2.54129, 8496, 57, 106 | 2.397, 34224, 54, 108 | 2.10853, 137187, 48, 95 | 2.2748, 549129, 52, 106 | 1.9168, 2.1968e+06, 44, 120 |
512 | 2.47646, 3953, 100, 215 | 2.17862, 16462, 95, 182 | 2.27822, 66717, 100, 213 | 1.99111, 267933, 87, 183 | 2.20756, 1.07294e+06, 97, 208 | 1.81953, 4.29301e+06, 80, 232 |
1024 | 0.705593, 7384, 54, 239 | 2.35119, 31976, 200, 466 | 1.91494, 131185, 165, 379 | 2.09636, 528822, 180, 427 | 1.80736, 2.12002e+06, 156, 369 | 1.69968, 8.48557e+06, 147, 487 |
2048 | 0.212693, 13420, 29, 273 | 1.44404, 61834, 236, 653 | 1.51211, 258618, 257, 718 | 1.55974, 1.04876e+06, 265, 754 | 1.52037, 4.21191e+06, 261, 752 | 1.41099, 1.6868e+07, 242, 1010 |
4096 | 0.311674, 22800, 77, 649 | 1.20733, 117308, 366, 1384 | 1.068, 507644, 346, 1231 | 1.07417, 2.08111e+06, 363, 1296 | 1.03939, 8.38687e+06, 356, 1297 | 0.815355, 3.36224e+07, 279, 1531 |
8192 | 0.949696, 32776, 354, 2214 | 0.783456, 213432, 436, 2239 | 0.797776, 985352, 507, 2572 | 0.659193, 4.1193e+06, 442, 2065 | 0.612422, 1.67044e+07, 417, 1953 | 0.446304, 6.70931e+07, 306, 1932 |
16384 | 0.815378, 33072, 281, 2261 | 0.490407, 360656, 442, 2893 | 0.352437, 1.8713e+06, 425, 2393 | 0.367647, 8.10146e+06, 481, 2754 | 0.343982, 3.32213e+07, 461, 2651 | 0.276707, 1.33893e+08, 376, 2596 |

Анализ результатов позволяет сделать ожидаемый вывод, что суммарный размер данных, выведенных на диск в процеесе всех операций записи при вставке значений в структуру существенно превосходит сам объем данных. Это вызвано тем, что для вставки одного элемента необходимо, в среднем, перезаписать на диске одну ячейку. Как видно из замеров, коэффициент отличия реально записанных данных от вставленных в структуру примерно соответствует вместимости ячейки.
Средняя скорость операций записи на диск при росте емкости ячеек беспредельно увеличивается, начиная с **6 Мб/с** для **32 байтов** и заканчивая неадекватно большими значениями. Природа таких значений, вероятно связана с работой дисковых кэшей. В целом, скорость записи на диск можно назвать приемлемой.


**Производительность чтения с диска в процессе операций вставки/поиска/удаления при разной емкости ячеек и для разного количества элементов**

Содержащиеся в ячейках представленной ниже таблицы значения представляют собой:
- скорость в Мб/с записи данных в структуру;
- суммарный размер в Кб всех данных, считываемых с диска за время вставки значений в структуру;
- скорость в Мб/с чтения данных с диска по отношению ко всему времени вставки элементов в структуру;
- скорость в Мб/с чтения данных с диска по отношению ко времени выполнения операций записи;

для соответствующих значений емкости ячейки и количества элементов.

Size of bucket \ Number of elements | 8192 | 32768 | 131072 | 524288 | 2097152 | 8388608
--- | --- | --- | --- |--- |--- |---
32 | 0.814589, 255, 2, 25 | 1.27819, 1023, 4, 35 | 2.00143, 4095, 6, 50 | 1.62257, 16382, 4, 43 | 1.49487, 65534, 4, 39 | 1.21176, 262142, 3, 25 |
64 | 2.1233, 508, 12, 94 | 2.14489, 2042, 12, 78 | 2.1607, 8185, 12, 72 | 1.89647, 32761, 11, 66 | 2.06226, 131064, 11, 72 | 1.70202, 524279, 9, 46 |
128 | 1.62588, 1006, 17, 135 | 2.84617, 4074, 31, 175 | 2.31608, 16358, 25, 130 | 2.22974, 65508, 25, 130 | 2.18692, 262112, 24, 126 | 1.83368, 1.04854e+06, 20, 82 |
256 | 2.82422, 1980, 60, 331 | 2.54129, 8108, 55, 281 | 2.397, 32667, 52, 250 | 2.10853, 130958, 46, 223 | 2.2748, 524159, 49, 235 | 1.9168, 2.09701e+06, 42, 160 |
512 | 2.47646, 3854, 97, 562 | 2.17862, 16078, 93, 492 | 2.27822, 65169, 97, 481 | 1.99111, 261718, 85, 413 | 2.20756, 1.04809e+06, 95, 439 | 1.81953, 4.19376e+06, 78, 306 |
1024 | 0.705593, 7288, 53, 544 | 2.35119, 31600, 198, 1141 | 1.91494, 129649, 163, 902 | 2.09636, 522622, 178, 930 | 1.80736, 2.09525e+06, 154, 727 | 1.69968, 8.38647e+06, 145, 606 |
2048 | 0.212693, 13326, 29, 641 | 1.44404, 61458, 234, 1523 | 1.51211, 257094, 255, 1565 | 1.55974, 1.04255e+06, 263, 1500 | 1.52037, 4.18729e+06, 260, 1355 | 1.41099, 1.67692e+07, 240, 1103 |
4096 | 0.311674, 22716, 77, 1417 | 1.20733, 116928, 365, 3010 | 1.068, 506080, 344, 2503 | 1.07417, 2.07494e+06, 362, 2443 | 1.03939, 8.36234e+06, 355, 2231 | 0.815355, 3.35241e+07, 279, 1637 |
8192 | 0.949696, 32704, 353, 4818 | 0.783456, 213064, 435, 4419 | 0.797776, 983816, 506, 4705 | 0.659193, 4.11318e+06, 442, 3432 | 0.612422, 1.66799e+07, 417, 2943 | 0.446304, 6.69951e+07, 305, 2211 |
16384 | 0.815378, 33008, 280, 5712 | 0.490407, 360288, 442, 5211 | 0.352437, 1.86978e+06, 425, 4303 | 0.367647, 8.0953e+06, 481, 4266 | 0.343982, 3.31965e+07, 461, 3688 | 0.276707, 1.33794e+08, 376, 3239 |

Анализ результатов позволяет сделать ожидаемый вывод, что суммарный размер данных, считанных с диска в процеесе всех операций чтения при вставке значений в структуру существенно превосходит сам объем данных. Это вызвано тем, что для вставки одного элемента необходимо, в среднем, перезаписать на диске одну ячейку. Как видно из замеров, коэффициент отличия реально считанных данных от вставленных в структуру примерно соответствует вместимости ячейки.
Средняя скорость операций чтения с диска при росте емкости ячеек беспредельно увеличивается, начиная с **4 Мб/с** для **32 байтов** и заканчивая неадекватно большими значениями. Природа таких значений, вероятно связана с работой дисковых кэшей. В целом, скорость чтения с диска можно назвать приемлемой.


**Спецефичные для структуры данных характеристики**

Содержащиеся в ячейках представленной ниже таблицы значения представляют собой:
- количество перемещений элементов при вставке значений в структуру;
- максимальная зафиксированная локальная глубина ячейки;
- размер массива директорий;
- средняя заполненность ячеек;

для соответствующих значений емкости ячейки и количества элементов.

Size of bucket \ Number of elements | 8192 | 32768 | 131072 | 524288 | 2097152 | 8388608
--- | --- | --- | --- |--- |--- |---
32 | 6026, 18, 3943, 0.69236 | 23803, 19, 15763, 0.692866 | 94804, 22, 63073, 0.692684 | 378385, 26, 252138, 0.693103 | 1.51415e+06, 28, 1.00873e+06, 0.692935 | 6.0479e+06, 30, 4.03077e+06, 0.693423 |
64 | 5949, 13, 1670, 0.700351 | 23691, 16, 6732, 0.695232 | 95059, 17, 27041, 0.69242 | 378034, 20, 108139, 0.692588 | 1.51106e+06, 22, 432170, 0.693161 | 6.04858e+06, 25, 1.72821e+06, 0.693127 |
128 | 6045, 11, 802, 0.680116 | 23929, 13, 3179, 0.686939 | 95604, 15, 12732, 0.686254 | 380596, 17, 50767, 0.688459 | 1.52204e+06, 20, 203070, 0.688412 | 6.08674e+06, 22, 811865, 0.688546 |
256 | 6038, 9, 381, 0.691775 | 24212, 11, 1554, 0.679743 | 96658, 13, 6228, 0.678776 | 385858, 16, 24916, 0.678737 | 1.54792e+06, 18, 99883, 0.677221 | 6.18544e+06, 20, 399168, 0.677626 |
512 | 6335, 8, 198, 0.653426 | 24399, 10, 768, 0.676347 | 97700, 12, 3097, 0.67156 | 391483, 14, 12432, 0.669334 | 1.56654e+06, 16, 49704, 0.669649 | 6.2542e+06, 18, 198505, 0.670493 |
1024 | 6122, 7, 97, 0.658203 | 23981, 9, 377, 0.682561 | 97914, 11, 1537, 0.671037 | 393950, 13, 6201, 0.665616 | 1.57345e+06, 15, 24768, 0.666616 | 6.29531e+06, 17, 99102, 0.66622 |
2048 | 6089, 6, 48, 0.655622 | 24116, 8, 189, 0.676305 | 97512, 10, 763, 0.67278 | 396183, 12, 3103, 0.662365 | 1.57133e+06, 14, 12313, 0.667803 | 6.29917e+06, 16, 49391, 0.665751 |
4096 | 5390, 5, 22, 0.697014 | 24456, 7, 96, 0.661065 | 100475, 9, 392, 0.652669 | 394461, 11, 1543, 0.664494 | 1.56753e+06, 13, 6133, 0.668995 | 6.28354e+06, 15, 24581, 0.667529 |
8192 | 4596, 4, 10, 0.727984 | 23518, 6, 47, 0.667298 | 98427, 8, 193, 0.660434 | 392444, 10, 767, 0.667302 | 1.56875e+06, 12, 3066, 0.668342 | 6.27028e+06, 14, 12253, 0.66889 |
16384 | 4121, 3, 5, 0.666992 | 23687, 5, 24, 0.640293 | 97272, 7, 96, 0.660111 | 393951, 9, 386, 0.661805 | 1.58381e+06, 11, 1548, 0.661331 | 6.29991e+06, 13, 6155, 0.665414 |

Анализ результатов измерений показывает, что удельное количество перемещений элементов в среднем постоянно и не превышает одного перемещенного на один вставленный. Локальная глубина ячеек, как и размер директорий тем больше, чем меньше размер ячейки. Средняя заполненность ячеек стабильно находится в районе **70%**.

**Директории**

Для оценки влияния реализации директорий на общую производительность были произведены тестовые запуски структуры данных с двумя различными реализациями директорий для двух различных хэш-функций.

Directories & Hasher | size of directories | counter of movings | max local depth | max bucket Id | average bucket filling 
--- | --- | --- | --- |--- |---
Map & Universal | 2090 | 66022 | 12 | 2089 | 0.759
Array & Universal | 4096 | 66022 | 12 | 2089 | 0.759
Map & Fractional (A = 0.066667) | 2116 | 66525 | 19 | 2115 | 0.75
Array & Fractional (A = 0.066667) | 524288 | 66525 | 19 | 2115 | 0.75

Результат анализа запусков позволяет сделать вывод, что реализация в виде массива оказывает существенное пагубное влияние на объем директорий только при выборе плохой хэш-функции.

## Вывод
В процессе выполнения работы была реализована структура данных "Расширяемое хэширование". В рамках модульного подхода к ее разработке были созданы несколько реализаций различных компонентов структуры данных. Анализ среднего времени выполнения операций, скорости работы с диском и специфичных для структуры данных характеристик позволил убедиться в верности или неверности предварительно выдвинутых гипотез о вычислительной сложности и эффективности работы структуры данных.  