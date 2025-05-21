Hướng dẫn chạy

Tạo tên database trên postgreSQl : smart-home ( Sau khi chạy backend sẽ ánh xạ lớp qua bảng sẽ tạo tự động các bảng  )

Sau khi cài đặt influxd

Tiến hành vào power shell chạy lệnh

cd -path 'C:\Program Files\influxdb2-2.7.11-windows'   
./influxd

![z6625644461609_99c1343d5ba7956d0cf95a65faac4652](https://github.com/user-attachments/assets/9a040142-5b94-4610-bce3-e58f18ddc581)

Sau khi cài đặt mosquitto

Vào cmd chạy lệnh
mosquitto –v

 ![z6625647197865_dbda0274bf7f51ff55ff267b7e20934e](https://github.com/user-attachments/assets/6e891abf-3c89-4023-b252-647540d37256)

Tiến hành chạy sửa sổ lần lượt device-simulator, socket-server,backend,frontend
![z6625647866120_3f204705bb438976b71cedd5ec02b375](https://github.com/user-attachments/assets/5cec80fb-98e7-43b2-aa55-d1a74480cc98)
 
Trên front end :
Xóa node_modules và package-lock.json

Chạy lệnh : 
npm install

npm run dev




