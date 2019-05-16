package com.example.enejwl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.enejwl.dlthgud.BackKeyClickHandler;
import com.example.enejwl.dlthgud.GameActivity;
import com.example.enejwl.dlthgud.Item;
import com.example.enejwl.dlthgud.Level;
import com.example.enejwl.dlthgud.Mole;

public class MainActivity extends AppCompatActivity {
    public static Activity mainActivity;
    private BackKeyClickHandler backKeyClickHandler;
    public static Item bomb = new Item("bomb", 1, 0, 5, 3, R.drawable.bomb,5);
    final static int MAX_LEVEL = 2;

    public static Level level[] = new Level[MAX_LEVEL + 1];
    public static Mole mole[] = new Mole[1];
    public static Item[] items = {bomb};
    int curLevel;
    public static int lastLevel = 1;



    RadioGroup radioGroup;
    RadioGroup end_mode;

    Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = MainActivity.this;
        backKeyClickHandler = new BackKeyClickHandler(this);

        init();
    }

    @Override
    public void onBackPressed() {
        backKeyClickHandler.onBackPressed();
    }

    private void init() {
        String package_name = getPackageName();

        start=findViewById(R.id.start);
        end_mode = (RadioGroup) findViewById(R.id.end_mode);

        // intent에서 "isWin" bool 값에 받기
        // 받기 실패 시 bool = -1
        int bool = getIntent().getIntExtra("isWin",-1);
        // intent에서 "curLevel" curLevel 값에 받기
        // 받기 실패 시 curLevel = 1
        curLevel = getIntent().getIntExtra("curLevel", 1);

        if (bool > -1){
            final ImageButton imageView = (ImageButton) findViewById(R.id.imageView);
            imageView.setVisibility(View.VISIBLE);
            if(bool == 0) {
                //  실패 화면
                start.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.loser);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView.setVisibility(View.GONE);
                        start.setVisibility(View.VISIBLE);
                    }
                });
            } else if(bool == 1) {
                //  성공 화면
                imageView.bringToFront();
                imageView.setImageResource(R.drawable.win);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView.setVisibility(View.GONE);
                    }
                });
                curLevel++; // 레벨 업
                if(curLevel > MAX_LEVEL) {
                    curLevel = MAX_LEVEL;
                }
                if(lastLevel < curLevel) {
                    lastLevel = curLevel;
                }
                end_mode.bringToFront();
            }
        }

        //  두더지 객체 생성
        mole[0] = new Mole("두더지", 1, 1, 2, 5, R.drawable.enejwl);


        //  아이템 객체 생성
//        Item[] items = {bomb};
        // 레벨 객체 생성
        int[] map_3 = new int[9];
        for (int i=0; i<9; i++) {
            map_3[i] = 1;
        }
//        int[] map_3 = {0,1,0,1,1,1,0,1,0};
//        int[] map_4 = {1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0};
        int[] map_5 = new int[25];
        for (int i=0; i<25; i++) {
            map_5[i] = 1;
        }
        level[1] = new Level(map_3, 3, 3, 40, 30, 0, mole, null);
        level[2] = new Level(map_5, 5, 5, 50, 40, 0, mole, items);

        radioGroup = (RadioGroup) findViewById(R.id.level);

        for(int i=1; i<=lastLevel; i++) {
            if(lastLevel > MAX_LEVEL) {
                break;
            }
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText("Level " + i);
            radioButton.setId(i);
            // getResources().getIdentifier("level_" + i, "id", package_name)
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT);
            radioGroup.addView(radioButton, layoutParams);
        }

        if(curLevel < 0) {
            curLevel = 1;
        }

        Toast.makeText(this, "레벨: " + curLevel, Toast.LENGTH_SHORT).show();

        if(curLevel == 0) {
            radioGroup.check(getResources().getIdentifier("user_mode", "id", package_name));
        } else {
            radioGroup.check(curLevel);
            // getResources().getIdentifier("level_" + curLevel, "id", package_name)
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = radioGroup.getCheckedRadioButtonId();
                Log.d("level", "onCheckedChanged: " + id);
                RadioButton radioButton = (RadioButton) findViewById(id);
                String level_text = radioButton.getText().toString();
                Log.d("level", "onCheckedChanged: " + level_text);
                String[] array = level_text.split(" ");
                Log.d("level", "onCheckedChanged: " + array[1]);
                if(array[1].equals("모드")) {
                    curLevel = 0;
                } else {
                    curLevel = Integer.parseInt(array[1]);
                }
//                Toast.makeText(getApplicationContext(), "레벨: " + curLevel, Toast.LENGTH_SHORT).show();
                if(curLevel == 0) {
                    end_mode.setVisibility(View.INVISIBLE);
                } else {
                    end_mode.setVisibility(View.VISIBLE);
                }
            }
        });

        start.setOnClickListener(new View.OnClickListener() {   // 시작 버튼 클릭 시
            @Override
            public void onClick(View v) {
                ImageButton imageButton = (ImageButton) findViewById(R.id.imageView);
                imageButton.setVisibility(View.GONE);
                if(curLevel != 0) {
                    int end_id = end_mode.getCheckedRadioButtonId();
                    RadioButton radioButton1 = (RadioButton) findViewById(end_id);
                    String end_text = radioButton1.getText().toString();
                    switch (end_text) {
                        case "시간 제한 방식":
                            level[curLevel].setEnd(0);
                            level[1].setCondition(40);
                            level[2].setCondition(50);
                            break;
                        case "놓친 두더지 방식":
                            level[curLevel].setEnd(1);
                            level[1].setCondition(5);
                            level[2].setCondition(3);
                            break;
                    }
                    // intent에서 "curLevel" curLevel 값 보내기
                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                    intent.putExtra("curLevel", curLevel);
                    startActivity(intent);  // GameActivity 전환
                } else {
                    CustomDialog customDialog = new CustomDialog(MainActivity.this);
                    customDialog.callFunction(level);
                    //  레벨 조건 입력
                }

            }
        });
    }
}

