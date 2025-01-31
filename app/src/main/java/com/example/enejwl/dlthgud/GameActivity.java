package com.example.enejwl.dlthgud;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enejwl.MainActivity;
import com.example.enejwl.R;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    TextView time;
    TextView scoreView;
    Thread[] threads;
    ArrayList arrayList = new ArrayList();

    final String[] sksdleh = {"Normal", "Hard", "Nightmare", "Korean"};
    String cursksdleh; // 현재 난이도

    int score; //현재점수
    int missed; // 놓친 두더지의 수
    int condition; // 총 제한 시간 또는 놓칠 수 있는 최대 두더지 수
    int count; //목표점수
    int width; //맵의 가로크기
    int height; //맵의 세로크기
    int[] map;  // 맵
    int end_method; // 종료 방식
    double minP;    // 주기
    double maxP;
    double p;   // 확률

    Mole[] moles;
    Item[] items;

    ImageButton[] hole;

    int a_second;   // 속도

    int level;  // 현재 레벨

    Thread thread = null;

    public static final int NONE = 0; // 안 하는 거
    public static final int EMPTY = 1;    // 사용하는 거

    // 종료 방식
    public final static int END_TIME = 0;
    public final static int END_COUNT = 1;

    final String TAG_NONE = "none";

    final int LOWER_BOUND = -1;
//    final int UPPER_BOUND = 3;

    final int MAX_HEIGHT = 500;

    boolean eBoolean = false;

    int isTouch = 0;    // 0: penalty X

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        play();
    }

    private void play() {
        if (init()) {
            threads = new Thread[width * height];
            hole = new ImageButton[width * height];

            // curLevel.map에 따라 hole 초기화

            int h = MAX_HEIGHT / height;
            final int WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            final int HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, getResources().getDisplayMetrics());
            final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    WIDTH, HEIGHT, 1f
            );

            int n = 0;
            for (int i = 0; i < height; i++) {
                LinearLayout linearLayout1 = new LinearLayout(this);
                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

                for (int j = 0; j < width; j++) {
                    final ImageButton button = new ImageButton(GameActivity.this);
                    button.setId(n + 1);
                    button.setLayoutParams(params);
                    button.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    hole[n] = button;

                    if (map[n] == EMPTY) {
                        hole[n].setVisibility(View.VISIBLE);
                        Log.v("태그", "그림1");
                        hole[n].setImageResource(R.drawable.ic_launcher_foreground);
                        //
                        ItemInfo itemInfo = new ItemInfo("0", 0, -1);
                        hole[n].setTag(itemInfo);
                    } else if (map[n] == NONE) {
                        hole[n].setVisibility(View.INVISIBLE);
                        hole[n].setTag(TAG_NONE);
                    }

                    hole[n].setOnClickListener(new View.OnClickListener() { //두더지이미지에 온클릭리스너
                        // 화면 터치시 터치 수를 감소시키고, 터치 수가 0이 되면 해당하는
//점수를 score에 더한다.
                        @Override

                        public void onClick(View v) {
                            ItemInfo info = (ItemInfo) ((ImageButton) v).getTag();

                            Log.d("난이도", "index : " + info.getIndex());
                            //  두더지? 폭탄?
                            if (((ImageButton) v).getTag().getClass().equals(MoleInfo.class)) { // 두더지
                                Toast.makeText(getApplicationContext(), "good", Toast.LENGTH_LONG).show();

                                boolean en = cursksdleh.equals(sksdleh[0])   // Normal
                                        || checkIsFirst(info);
                                if (isTouch == 0
                                        || en) {
                                    //  터치 수 --
                                    info.setTouch(info.getTouch() - 1);
                                    Log.d("터치 수", "onClick: " + info.getTouch());
                                } else if (isTouch == 1) {
                                    Log.d("터치 수", "onClick: penalty");
                                    info.setTouch(0);
                                }
                                //  터치 수 = 0?
                                if (info.getTouch() == 0) {
//  score += 점수
                                    if (en) {    // 또는 가장 먼저
                                        score += ((MoleInfo) info).getScore();  // 점수를 얻고
                                    } else {
                                        if (cursksdleh.equals(sksdleh[1])) { // Hard 난이도
                                            for (int t=0; t<moles.length; t++) {
                                                if (info.getName().equals(moles[t].getName())) {
                                                    info.setTouch(moles[t].getTouch());
                                                    break;
                                                }
                                            }
                                            return; // 아무 반응이 없습니다
                                        }
                                        if (cursksdleh.equals(sksdleh[3])) { // Korean 난이도
                                            score -= ((MoleInfo) info).getScore();  // 점수 감소
                                            // 줄어듭니다
                                            decrease(1);
                                        }
                                    }

                                    scoreView.setText(String.valueOf(score) + "점");
                                    Log.v("태그", "그림2");
                                    // hole 초기화
                                    holeInit(v, info.getIndex());

                                    Log.d("난이도", "제대로 클릭 빼기 : " + String.valueOf(info.getIndex()));
                                }

                            } else if (((ImageButton) v).getTag().getClass().equals(ItemInfo.class)) {  // 아이템
                                Toast.makeText(getApplicationContext(), info.getName(), Toast.LENGTH_LONG).show();
                                int intname = getIntname(info);

                                boolean dk = cursksdleh.equals(sksdleh[0]) || cursksdleh.equals(sksdleh[1])  // Normal 이나 Hard
                                        || intname == 1  // 또는 비상벨
                                        || checkIsFirst(info);
                                if (isTouch == 0
                                        || dk) {
                                    //  터치 수 --
                                    info.setTouch(info.getTouch() - 1);
                                    Log.d("터치 수", "onClick: " + info.getTouch());
                                } else if (isTouch == 1) {
                                    Log.d("터치 수", "onClick: penalty");
                                    info.setTouch(0);
                                }
                                //  터치 수 = 0?
                                if (info.getTouch() == 0) {
                                    if (dk) {    // 또는 가장 먼저
                                        int hap = bomb(intname);
                                        score += (hap * items[intname].getScore());
                                    }

                                    scoreView.setText(String.valueOf(score) + "점");
                                    Log.v("태그", "그림2");
                                    // hole 초기화
                                    holeInit(v, info.getIndex());

                                    Log.d("난이도", "제대로 클릭 빼기 : " + String.valueOf(info.getIndex()));
                                }
                            }
                        }
                    });
                    linearLayout1.addView(hole[n]);

                    n++;
                }

                linearLayout.addView(linearLayout1);
            }

            // 사용자가 제한시간 방식을
            //선택했으면
            if (end_method == END_TIME) {
//            // timeCheck스레드, AThread 스레드와 IThread 시작 및 onClick
                thread = new Thread(new GameActivity.timeCheck(condition));
                thread.start();

//            //함수를 호출
            }

            // 놓친 두더지 방식을 선택했으면
            else if (end_method == END_COUNT) {
                time.setText(condition + "마리");
                // AThread와 IThread 시작과
                //onClick 함수만 호출
            }
            // 쓰레드 시작
            new Thread(new PThread(minP, maxP, p)).start(); // 각 레벨별 정보 입력해야 함.
            //두더지 주기 최소, 최대, 아이템과 두더지 비율 순으로 입력하기!!!
        }
    }

    private void holeInit(View v, int index) {   // hole 초기화
        ((ImageButton) v).setImageResource(R.drawable.ic_launcher_foreground);
        //
        arrayList.remove(String.valueOf(index));

        ItemInfo itemInfo = new ItemInfo("0", 0, -1);
        v.setTag(itemInfo);

        if (index != -1 && threads[index] != null) {
            System.out.println(index);
            threads[index].interrupt();

        }
        ////////////나중에 하기 쓰레드
    }

    private int getIntname(ItemInfo info) {  // 아이템 번호
        if (items == null) {
            return -1;
        } else {
            for (int i = 0; i < items.length; i++) {
                if (info.getName().equals(items[i].getName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void decrease(int n) {   // 놓칠 수 있는 두더지 수
        if (end_method == END_COUNT) {  // 놓친 두더지 수 방식일 경우
            missed += n;   // 놓친 것으로 카운트

            time.setText(condition - missed + "마리");
            if (missed == condition) {
                end(score); // 종료 함수 호출
            }
        }
    }

    private boolean init() {
        time = findViewById(R.id.time);

        scoreView = findViewById(R.id.count);
        scoreView.setText(0 + "점");

        a_second = 1000;

        level = getIntent().getIntExtra("curLevel", -1);
        if (level > LOWER_BOUND && level < MainActivity.level.length) {

            cursksdleh = sksdleh[getIntent().getIntExtra("cursksdleh", 0)];
            final Level curLevel = MainActivity.level[level];
            Log.d("등장 확률", "play: " + curLevel.getP());

            // 게임 시작시 필요한 조건을 준비
            score = 0;
            missed = 0;
            condition = curLevel.getCondition();
            count = curLevel.getMoleNum();
            width = curLevel.getWidth();
            height = curLevel.getHeight();
            map = curLevel.getMap();
            end_method = curLevel.getEnd();
            moles = curLevel.getMoleArr();
            items = curLevel.getItemArr();
            minP = curLevel.getMinP();
            maxP = curLevel.getMaxP();
            p = curLevel.getP();

            return true;
        } else return false;
    }

    class timeCheck implements Runnable {   // 시간을 1초씩 감소시키는 쓰레드 클래스
        int MAXTIME;    // 총 게임 시간을 저장
        Message message; // i를 전달할 메세지 객체

        public timeCheck(int i) {
            this.MAXTIME = i;
        }

        @Override
        public void run() { // MAXTIME부터 0이 될 때까지 1초에 1씩 감소시키는 함수이다.
            for (int i = MAXTIME; i >= 0; i--) {
                message = new Message();    // 객체 생성
                message.arg1 = i;
                timeHandler.sendMessage(message);   // 시간 출력
                try {
                    Thread.sleep(a_second);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }

            end(score);
            finish();   // 종료
        }
    }

    Handler timeHandler = new Handler() {    // TextView에 시간을 출력하는 Handler 객체
        @Override
        public void handleMessage(Message msg) {
            time.setText(msg.arg1 + "초");

//            if(msg.arg1 == 0){
//                thread.interrupt();
//            }
        }
    };


    /* 구멍에서 두더지가 올라갔다 내려가는 함수 */
    public class AThread implements Runnable {

        int index = 0; // 나오는 구멍 번호

        AThread(int index) {
            this.index = index;
        }

        @Override

        public void run() {
            try {
                int anum = 0;
                int length = moles.length; // 디비

                Message msg1 = new Message();
                Message msg2 = new Message();
                double min = moles[anum].getUpMin(); // 올라와 있는 최소
                double max = moles[anum].getUpMax(); // 올라와 있는 시간 최대
                Log.d("올라와있는 시간", "run: " + min + " " + max);
                int uptime = (int) ((min + new Random().nextDouble() * (max - min)) * 1000); // 두더지 올라와 있는 시간

                ItemInfo info1 = (ItemInfo) ((ImageButton) hole[index]).getTag();
                if (info1.getName().equals("0")) {
                    msg1.arg1 = index;
                    msg1.obj = moles[anum];
                    upHandler.sendMessage(msg1);
                    Thread.sleep(uptime);
                }



                ItemInfo info2 = (ItemInfo) ((ImageButton) hole[index]).getTag();

                if (!info2.getName().equals("0")) {
                    msg2.arg1 = index;
                    msg2.obj = moles[anum];
                    downHandler.sendMessage(msg2);

                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("쓰레드 죽었음");
            }
            return;
        }

    }

    /* 아이템 올라갔다 내려가는 함수 */
    public class IThread implements Runnable {
        int index = 0; //

        IThread(int index) {
            this.index = index;
        }

        @Override

        public void run() {
            try {
                int length = items.length; //item 배열의 길이
                Message msg1 = new Message();
                Message msg2 = new Message();

                double min = items[0].getUpMin();
                double max = items[0].getUpMax();
                Log.d("올라와있는 시간", "run: " + min + " " + max);
                int uptime = (int) ((min + new Random().nextDouble() * (max - min)) * 1000); // 아이템 올라가 있는 시간

                if (length > 1) { //아이템이 2개 이상일 때 나올 확률별로 나오게 하기
                    if (makeRandom(0.5)) {
                        msg1.arg1 = index;
                        msg1.obj = items[0];
                        msg2.arg1 = index;
                        msg2.obj = items[0];

                    } else {
                        msg1.arg1 = index;
                        msg1.obj = items[1];
                        msg2.arg1 = index;
                        msg2.obj = items[1];

                    }
                } else { //아니면 그냥 나오게 하기
                    msg1.arg1 = index;
                    msg1.obj = items[0];
                    msg2.arg1 = index;
                    msg2.obj = items[0];

                }
                ItemInfo info1 = (ItemInfo) ((ImageButton) hole[index]).getTag();
                if (info1.getName().equals("0")) {
                    upHandler.sendMessage(msg1);
                    Thread.sleep(uptime);
                }


                ItemInfo info2 = (ItemInfo) ((ImageButton) hole[index]).getTag();
                // Korean 난이도에서는 비상벨이 스스로 사라지지 않고 버틴다.
                if ((!info2.getName().equals("0") && !(info2.getName().equals("bell") && cursksdleh.equals(sksdleh[3])))) {
                    downHandler.sendMessage(msg2);

                }



            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("쓰레드 죽었음");
            }

            return;
        }

    }

    /* 아이템, 두더지 올라갔다 내려가는 함수 - 총 관리하는 함수 */
    public class PThread implements Runnable {
        double minP; // 주기 최소 시간
        double maxP; // 주기 최대 시간
        double possibility;

        PThread(double minP, double maxP, double possibility) {
            this.minP = minP;
            this.maxP = maxP;
            this.possibility = possibility;
        }

        @Override

        public void run() {

            while (true) {

                try {
                    int position = 0; // 구멍 위치 정보 저장하는 변수
                    int period = (int) ((minP + new Random().nextDouble() * (maxP - minP)) * 1000); // 주기

                    /* 맵 상에서 두더지 아이템 아무것도 없는 칸 찾기 */
                    while (true) {
                        position = new Random().nextInt(hole.length);
//                        Log.v("position", "position 반복문 안 " + position);

                        if (map[position] == NONE) { // 사용하지 않는 구멍
                            continue;
                        }
                        ItemInfo info = (ItemInfo) ((ImageButton) hole[position]).getTag();
                        if (info.getName().equals("0"))  // 빈 구멍
                            break;
                    }

//                    Log.d("난이도", "배열 넣기 : " + arrayList.get(arrayList.size() - 1));

                    if (items == null) { // 아이템이 없는 경우
                        Log.v("쓰레드", "잘나옴");
                        // 두더지 쓰레드 생성
                        arrayList.add(String.valueOf(position)); // 배열 넣기

                        threads[position] = new Thread(new AThread(position));
                        threads[position].start();

                        Log.v("쓰레드", "두더지쓰레드 생성");
                    } else {  // 아이템이 있는 경우 - 두더지 쓰레드와 아이템 쓰레드 비율에 맞게 생성
                        if (makeRandom(possibility)) {
                            if (!cursksdleh.equals(sksdleh[1])) {
                                arrayList.add(String.valueOf(position)); // 배열 넣기
                            }
                            threads[position] = new Thread(new IThread(position)); //아이템일 때
                            threads[position].start();
                            Log.v("쓰레드", "아이템쓰레드 생성");
                        } else {
                            arrayList.add(String.valueOf(position)); // 배열 넣기
                            threads[position] = new Thread(new AThread(position)); //아이템일 때
                            threads[position].start();
                            Log.v("쓰레드", "두더지쓰레드 생성");

                        }
                    }

                    Thread.sleep(period); // 잠들기

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (eBoolean) return;
            }
        }
    }

    Handler upHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.obj.getClass() == Mole.class) { // 올라온 것이 두더지 일 때
                Mole amole = (Mole) msg.obj;
                MoleInfo moleInfo = new MoleInfo(amole.getName(), amole.getTouch(), amole.getScore(), msg.arg1);
                hole[msg.arg1].setTag(moleInfo);
                hole[msg.arg1].setImageResource(amole.getImage()); // 이미지 바꿀 수 있어야 함.
            } else { // 올라온 것이 아이템
                Item aitem = (Item) msg.obj;
                ItemInfo itemInfo = new ItemInfo(aitem.getName(), aitem.getTouch(), msg.arg1);
                hole[msg.arg1].setTag(itemInfo);
                hole[msg.arg1].setImageResource(aitem.getImage());
            }
            //  아이템일 때 필요!
        }
    };

    Handler downHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // hole 초기화
            holeInit(hole[msg.arg1], msg.arg1);

            Log.d("난이도", "뺀 것 : " + arrayList.contains(String.valueOf(msg.arg1)));
            if (msg.obj.getClass() == Mole.class) {
                // 줄어듭니다
                decrease(1);

            }
        }
    };

    @Override
    public void onBackPressed() {

    }

    private int end(int score) {    // 게임이 끝났을 때 승패 판별을 담당하는 함수이다.
        MainActivity mainActivity = (MainActivity) MainActivity.mainActivity;
        mainActivity.finish();

        eBoolean = true;

        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        intent.putExtra("curLevel", level);
        intent.putExtra("end", end_method);
        int c = 0;
        for (int i = 0; i < 4; i++) {
            if (cursksdleh.equals(sksdleh[i])) {
                c = i;
            }
        }
        intent.putExtra("cursksdleh", c);

        if (score >= count) {
            intent.putExtra("isWin", 1);

            startActivity(intent);
            return 1;
        } else {
            intent.putExtra("isWin", 0);

            startActivity(intent);
            return 0;
        }
    }

    private boolean makeRandom(double num) {
        double result = 0;
        double random = Math.random(); //0~1 실수
        if (random <= num) {
            return true; //특정 확률 안이면 true
        } else {
            return false; //특정 확률 밖이면 false
        }
    }

    private boolean checkIsFirst(ItemInfo info) {
        if (arrayList.size() != 0 && String.valueOf(info.getIndex()).equals(arrayList.get(0)))
            return true;
        else return false;
    }

    private int bomb(int b) {    // 폭탄 아이템을 사용할 때 맵 위에 있는 두더지의 총 점수를 계산하고         더하는 함수
        int hap = 0;  // 아이템을 사용해서 얻는 점수를 저장

        for (int i = 0; i < hole.length; i++) {

            if (((ImageButton) hole[i]).getTag().getClass().equals(MoleInfo.class)) {
                Toast.makeText(getApplicationContext(), "good", Toast.LENGTH_LONG).show();
                // 줄어듭니다
                decrease(items[b].getPeriod());

//  score += 점수
                MoleInfo moleInfo = (MoleInfo) hole[i].getTag();
                hap += moleInfo.getScore();
                // hole 초기화
                holeInit(hole[i], i);
            }

        }
        return hap;
    }
}