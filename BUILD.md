# Build and Run
## 1. Clone away
```
$ mkdir <ts-project>
$ cd <ts-project>
$ git clone git://git.yoctoproject.org/meta-tensorflow
$ git clone git://git.openembedded.org/meta-openembedded
$ git clone git://git.openembedded.org/openembedded-core oe-core
$ cd oe-core
$ git clone git://git.openembedded.org/bitbake
```

## 2. Prepare build
```
$ . <ts-project>/oe-core/oe-init-build-env <build>

# Build qemux86-64 which runqemu supports kvm.
$ echo 'MACHINE = "qemux86-64"' >> conf/local.conf

$ echo 'IMAGE_INSTALL_append = " tensorflow"' >> conf/local.conf

Edit conf/bblayers.conf to include other layers
BBLAYERS ?= " \
    <ts-project>/oe-core/meta \
    <ts-project>/meta-openembedded/meta-python \
    <ts-project>/meta-openembedded/meta-oe \
    <ts-project>/meta-tensorflow \
"
```

## 3. Build image
```
cd <build>
$ bitbake core-image-minimal
```

## 4. Start qemu with slrip + kvm + 5GB memory:
```
$ runqemu qemux86-64 core-image-minimal slirp kvm qemuparams="-m 5120"
```

## 5. Verify the install
```
root@qemux86-64:~# python3 -c "import tensorflow as tf;print(tf.reduce_sum(tf.random.normal([1000, 1000])))"
tf.Tensor(-3304.6208, shape=(), dtype=float32)
```

## 6. Run tutorial case
### Refer: https://www.tensorflow.org/tutorials
```
root@qemux86-64:~# cat >code-v2.py <<ENDOF
import tensorflow as tf
mnist = tf.keras.datasets.mnist

(x_train, y_train), (x_test, y_test) = mnist.load_data()
x_train, x_test = x_train / 255.0, x_test / 255.0

model = tf.keras.models.Sequential([
  tf.keras.layers.Flatten(input_shape=(28, 28)),
  tf.keras.layers.Dense(128, activation='relu'),
  tf.keras.layers.Dropout(0.2),
  tf.keras.layers.Dense(10)
])

predictions = model(x_train[:1]).numpy()
tf.nn.softmax(predictions).numpy()
loss_fn = tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True)
loss_fn(y_train[:1], predictions).numpy()

model.compile(optimizer='adam',
              loss=loss_fn,
              metrics=['accuracy'])
model.fit(x_train, y_train, epochs=5)
model.evaluate(x_test,  y_test, verbose=2)

probability_model = tf.keras.Sequential([
  model,
  tf.keras.layers.Softmax()
])
probability_model(x_test[:5])


ENDOF

root@qemux86-64:~# python3 ./code-v2.py
2020-12-15 08:16:44.171593: I tensorflow/compiler/mlir/mlir_graph_optimization_pass.cc:116] None of the MLIR optimization passes are enabled (registered 2)
2020-12-15 08:16:44.184464: I tensorflow/core/platform/profile_utils/cpu_utils.cc:112] CPU Frequency: 3099995000 Hz
Epoch 1/5
1875/1875 [==============================] - 14s 7ms/step - loss: 0.4833 - accuracy: 0.8595
Epoch 2/5
1875/1875 [==============================] - 13s 7ms/step - loss: 0.1549 - accuracy: 0.9558
Epoch 3/5
1875/1875 [==============================] - 13s 7ms/step - loss: 0.1135 - accuracy: 0.9651
Epoch 4/5
1875/1875 [==============================] - 13s 7ms/step - loss: 0.0889 - accuracy: 0.9729
Epoch 5/5
1875/1875 [==============================] - 13s 7ms/step - loss: 0.0741 - accuracy: 0.9777
313/313 - 1s - loss: 0.0757 - accuracy: 0.9757
```
## 7. TensorFlow/TensorFlow Lite C++ Image Recognition Demo
```
root@qemux86-64:~# time label_image
2020-12-15 08:18:34.853885: I tensorflow/core/platform/profile_utils/cpu_utils.cc:112] CPU Frequency: 3099995000 Hz
2020-12-15 08:18:41.565167: I tensorflow/examples/label_image/main.cc:252] military uniform (653): 0.834306
2020-12-15 08:18:41.567874: I tensorflow/examples/label_image/main.cc:252] mortarboard (668): 0.0218696
2020-12-15 08:18:41.568936: I tensorflow/examples/label_image/main.cc:252] academic gown (401): 0.0103581
2020-12-15 08:18:41.569985: I tensorflow/examples/label_image/main.cc:252] pickelhaube (716): 0.00800819
2020-12-15 08:18:41.571025: I tensorflow/examples/label_image/main.cc:252] bulletproof vest (466): 0.00535086

real	0m7.178s
user	0m6.101s
sys	0m0.893s

root@qemux86-64:~# time label_image.lite
INFO: Loaded model /usr/share/label_image/mobilenet_v1_1.0_224_quant.tflite
INFO: resolved reporter
INFO: invoked
INFO: average time: 213.584 ms
INFO: 0.780392: 653 military uniform
INFO: 0.105882: 907 Windsor tie
INFO: 0.0156863: 458 bow tie
INFO: 0.0117647: 466 bulletproof vest
INFO: 0.00784314: 835 suit

real	0m0.233s
user	0m0.216s
sys	0m0.012s
```
