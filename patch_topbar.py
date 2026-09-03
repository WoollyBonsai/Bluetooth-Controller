with open('app/src/main/res/layout/activity_main.xml', 'r') as f:
    content = f.read()

# Make tvStatus width 0dp
content = content.replace(
"""        <TextView
            android:id="@+id/tvStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginStart="8dp"
            android:text="Disconnected"
            android:layout_weight="1" />""",
"""        <TextView
            android:id="@+id/tvStatus"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:layout_marginStart="4dp"
            android:text="Disconnected"
            android:layout_weight="1" />"""
)

# And fix button layout weights to ensure they are visible
content = content.replace(
"""        <Button
            android:id="@+id/btnInit"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Broadcast" />

        <Button
            android:id="@+id/btnConnect"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Connect" />""",
"""        <Button
            android:id="@+id/btnInit"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Init" />

        <Button
            android:id="@+id/btnConnect"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Conn" />"""
)

with open('app/src/main/res/layout/activity_main.xml', 'w') as f:
    f.write(content)
